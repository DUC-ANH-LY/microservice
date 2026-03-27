package com.example.saga.orchestration.orchestrator.core;

import com.example.saga.orchestration.orchestrator.api.SagaController;
import com.example.saga.shared.contracts.FailAt;
import com.example.saga.shared.contracts.SagaStartRequest;
import com.example.saga.shared.contracts.orchestration.*;
import com.example.saga.shared.contracts.topic.OrchestrationTopicNames;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SagaOrchestrator {
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ConcurrentHashMap<UUID, SagaInstance> store = new ConcurrentHashMap<>();

  public SagaOrchestrator(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public UUID startSaga(SagaStartRequest request) {
    UUID sagaId = UUID.randomUUID();
    UUID correlationId = UUID.randomUUID();
    FailAt failAt = request.getFailAt();

    SagaInstance instance = new SagaInstance(sagaId, request.getOrderId(), correlationId, failAt);
    instance.setStatus(SagaStatus.IN_PROGRESS);
    instance.setStep("PAYMENT", "PENDING");
    instance.setStep("INVENTORY", "PENDING");
    instance.setStep("SHIPMENT", "PENDING");
    store.put(sagaId, instance);

    ProcessPaymentCommand cmd = new ProcessPaymentCommand(sagaId, request.getOrderId(), correlationId, failAt);
    kafkaTemplate.send(OrchestrationTopicNames.PAYMENT_PROCESS_COMMAND, sagaId.toString(), cmd);
    return sagaId;
  }

  public SagaController.SagaStatusResponse getStatus(UUID sagaId) {
    SagaInstance instance = store.get(sagaId);
    if (instance == null) {
      // Keep response shape stable; this is a demo, so we don't use 404.
      return new SagaController.SagaStatusResponse(sagaId, SagaStatus.FAILED.name(), List.of());
    }

    List<SagaController.StepStatus> steps = new ArrayList<>();
    for (Map.Entry<String, String> e : instance.getSteps().entrySet()) {
      steps.add(new SagaController.StepStatus(e.getKey(), e.getValue()));
    }
    return new SagaController.SagaStatusResponse(instance.getSagaId(), instance.getStatus().name(), steps);
  }

  @KafkaListener(
      topics = OrchestrationTopicNames.PAYMENT_SUCCEEDED_EVENT,
      groupId = "orchestration-orchestrator"
  )
  public void onPaymentSucceeded(PaymentSucceededEvent event) {
    SagaInstance instance = requireInstance(event.getSagaId());
    instance.setStep("PAYMENT", "SUCCEEDED");

    ReserveInventoryCommand cmd = new ReserveInventoryCommand(
        event.getSagaId(),
        event.getOrderId(),
        event.getCorrelationId(),
        instance.getFailAt()
    );
    instance.setStep("INVENTORY", "PENDING");
    kafkaTemplate.send(OrchestrationTopicNames.INVENTORY_RESERVE_COMMAND, event.getSagaId().toString(), cmd);
  }

  @KafkaListener(
      topics = OrchestrationTopicNames.PAYMENT_FAILED_EVENT,
      groupId = "orchestration-orchestrator"
  )
  public void onPaymentFailed(PaymentFailedEvent event) {
    SagaInstance instance = requireInstance(event.getSagaId());
    instance.setStep("PAYMENT", "FAILED");
    instance.setStatus(SagaStatus.FAILED);
  }

  @KafkaListener(
      topics = OrchestrationTopicNames.INVENTORY_RESERVED_EVENT,
      groupId = "orchestration-orchestrator"
  )
  public void onInventoryReserved(InventoryReservedEvent event) {
    SagaInstance instance = requireInstance(event.getSagaId());
    instance.setStep("INVENTORY", "SUCCEEDED");

    ShipOrderCommand cmd = new ShipOrderCommand(
        event.getSagaId(),
        event.getOrderId(),
        event.getCorrelationId(),
        instance.getFailAt()
    );
    instance.setStep("SHIPMENT", "PENDING");
    kafkaTemplate.send(OrchestrationTopicNames.SHIPPING_SHIP_COMMAND, event.getSagaId().toString(), cmd);
  }

  @KafkaListener(
      topics = OrchestrationTopicNames.INVENTORY_FAILED_EVENT,
      groupId = "orchestration-orchestrator"
  )
  public void onInventoryFailed(InventoryReserveFailedEvent event) {
    SagaInstance instance = requireInstance(event.getSagaId());
    instance.setStep("INVENTORY", "FAILED");

    // Compensation: payment refund.
    instance.setStep("REFUND", "IN_PROGRESS");
    instance.setCompensationsPending(1);
    RefundCommand refundCommand = new RefundCommand(event.getSagaId(), event.getOrderId(), event.getCorrelationId(), instance.getFailAt());
    kafkaTemplate.send(OrchestrationTopicNames.PAYMENT_REFUND_COMMAND, event.getSagaId().toString(), refundCommand);
  }

  @KafkaListener(
      topics = OrchestrationTopicNames.SHIPMENT_SHIPPED_EVENT,
      groupId = "orchestration-orchestrator"
  )
  public void onShipmentShipped(ShipmentShippedEvent event) {
    SagaInstance instance = requireInstance(event.getSagaId());
    instance.setStep("SHIPMENT", "SUCCEEDED");
    instance.setStatus(SagaStatus.COMPLETED);
  }

  @KafkaListener(
      topics = OrchestrationTopicNames.SHIPMENT_FAILED_EVENT,
      groupId = "orchestration-orchestrator"
  )
  public void onShipmentFailed(ShipmentFailedEvent event) {
    SagaInstance instance = requireInstance(event.getSagaId());
    instance.setStep("SHIPMENT", "FAILED");

    // Compensation: release inventory + refund payment.
    instance.setStep("INVENTORY_RELEASE", "IN_PROGRESS");
    instance.setStep("REFUND", "IN_PROGRESS");
    instance.setCompensationsPending(2);

    ReleaseInventoryCommand releaseCommand = new ReleaseInventoryCommand(event.getSagaId(), event.getOrderId(), event.getCorrelationId(), instance.getFailAt());
    RefundCommand refundCommand = new RefundCommand(event.getSagaId(), event.getOrderId(), event.getCorrelationId(), instance.getFailAt());

    kafkaTemplate.send(OrchestrationTopicNames.INVENTORY_RELEASE_COMMAND, event.getSagaId().toString(), releaseCommand);
    kafkaTemplate.send(OrchestrationTopicNames.PAYMENT_REFUND_COMMAND, event.getSagaId().toString(), refundCommand);
  }

  @KafkaListener(
      topics = OrchestrationTopicNames.PAYMENT_REFUND_COMPLETED_EVENT,
      groupId = "orchestration-orchestrator"
  )
  public void onRefundCompleted(RefundCompletedEvent event) {
    SagaInstance instance = requireInstance(event.getSagaId());
    instance.setStep("REFUND", "COMPLETED");

    int pending = instance.decrementCompensationsPending();
    if (pending == 0 && instance.getStatus() != SagaStatus.COMPLETED) {
      instance.setStatus(SagaStatus.FAILED);
    }
  }

  @KafkaListener(
      topics = OrchestrationTopicNames.INVENTORY_RELEASED_EVENT,
      groupId = "orchestration-orchestrator"
  )
  public void onInventoryReleased(InventoryReleasedEvent event) {
    SagaInstance instance = requireInstance(event.getSagaId());
    instance.setStep("INVENTORY_RELEASE", "COMPLETED");

    int pending = instance.decrementCompensationsPending();
    if (pending == 0 && instance.getStatus() != SagaStatus.COMPLETED) {
      instance.setStatus(SagaStatus.FAILED);
    }
  }

  private SagaInstance requireInstance(UUID sagaId) {
    SagaInstance instance = store.get(sagaId);
    if (instance == null) {
      throw new IllegalStateException("Unknown sagaId " + sagaId);
    }
    return instance;
  }
}

