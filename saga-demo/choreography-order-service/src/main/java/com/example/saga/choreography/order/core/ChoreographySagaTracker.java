package com.example.saga.choreography.order.core;

import com.example.saga.choreography.order.api.SagaController;
import com.example.saga.shared.contracts.FailAt;
import com.example.saga.shared.contracts.SagaStartRequest;
import com.example.saga.shared.contracts.orchestration.*;
import com.example.saga.shared.contracts.topic.ChoreographyTopicNames;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChoreographySagaTracker {
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ConcurrentHashMap<UUID, ChoreographySagaInstance> store = new ConcurrentHashMap<>();

  public ChoreographySagaTracker(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public UUID startSaga(SagaStartRequest request) {
    UUID sagaId = UUID.randomUUID();
    UUID correlationId = UUID.randomUUID();
    FailAt failAt = request.getFailAt();

    ChoreographySagaInstance instance = new ChoreographySagaInstance(sagaId, request.getOrderId(), correlationId, failAt);
    instance.setStatus(ChoreographySagaStatus.IN_PROGRESS);
    instance.setStep("PAYMENT", "PENDING");
    instance.setStep("INVENTORY", "PENDING");
    instance.setStep("SHIPMENT", "PENDING");
    store.put(sagaId, instance);

    OrderCreatedEvent created = new OrderCreatedEvent(sagaId, request.getOrderId(), correlationId, failAt);
    kafkaTemplate.send(ChoreographyTopicNames.ORDER_CREATED_EVENT, sagaId.toString(), created);
    return sagaId;
  }

  public SagaController.SagaStatusResponse getStatus(UUID sagaId) {
    ChoreographySagaInstance instance = store.get(sagaId);
    if (instance == null) {
      return new SagaController.SagaStatusResponse(sagaId, ChoreographySagaStatus.FAILED.name(), List.of());
    }

    List<SagaController.StepStatus> steps = new ArrayList<>();
    for (Map.Entry<String, String> e : instance.getSteps().entrySet()) {
      steps.add(new SagaController.StepStatus(e.getKey(), e.getValue()));
    }
    return new SagaController.SagaStatusResponse(instance.getSagaId(), instance.getStatus().name(), steps);
  }

  @KafkaListener(topics = ChoreographyTopicNames.PAYMENT_SUCCEEDED_EVENT, groupId = "choreography-order-service")
  public void onPaymentSucceeded(PaymentSucceededEvent event) {
    ChoreographySagaInstance instance = requireInstance(event.getSagaId());
    instance.setStep("PAYMENT", "SUCCEEDED");
    instance.setStep("INVENTORY", "PENDING");
  }

  @KafkaListener(topics = ChoreographyTopicNames.PAYMENT_FAILED_EVENT, groupId = "choreography-order-service")
  public void onPaymentFailed(PaymentFailedEvent event) {
    ChoreographySagaInstance instance = requireInstance(event.getSagaId());
    instance.setStep("PAYMENT", "FAILED");
    instance.setStatus(ChoreographySagaStatus.FAILED);
  }

  @KafkaListener(topics = ChoreographyTopicNames.INVENTORY_RESERVED_EVENT, groupId = "choreography-order-service")
  public void onInventoryReserved(InventoryReservedEvent event) {
    ChoreographySagaInstance instance = requireInstance(event.getSagaId());
    instance.setStep("INVENTORY", "SUCCEEDED");
    instance.setStep("SHIPMENT", "PENDING");
  }

  @KafkaListener(topics = ChoreographyTopicNames.INVENTORY_FAILED_EVENT, groupId = "choreography-order-service")
  public void onInventoryFailed(InventoryReserveFailedEvent event) {
    ChoreographySagaInstance instance = requireInstance(event.getSagaId());
    instance.setStep("INVENTORY", "FAILED");

    // Compensation: refund (payment service will emit REFUND_COMPLETED_EVENT).
    instance.setStep("REFUND", "IN_PROGRESS");
    instance.setCompensationsPending(1);
  }

  @KafkaListener(topics = ChoreographyTopicNames.SHIPMENT_SHIPPED_EVENT, groupId = "choreography-order-service")
  public void onShipmentShipped(ShipmentShippedEvent event) {
    ChoreographySagaInstance instance = requireInstance(event.getSagaId());
    instance.setStep("SHIPMENT", "SUCCEEDED");
    instance.setStatus(ChoreographySagaStatus.COMPLETED);
  }

  @KafkaListener(topics = ChoreographyTopicNames.SHIPMENT_FAILED_EVENT, groupId = "choreography-order-service")
  public void onShipmentFailed(ShipmentFailedEvent event) {
    ChoreographySagaInstance instance = requireInstance(event.getSagaId());
    instance.setStep("SHIPMENT", "FAILED");

    // Compensation: release inventory + refund.
    instance.setStep("INVENTORY_RELEASE", "IN_PROGRESS");
    instance.setStep("REFUND", "IN_PROGRESS");
    instance.setCompensationsPending(2);
  }

  @KafkaListener(topics = ChoreographyTopicNames.REFUND_COMPLETED_EVENT, groupId = "choreography-order-service")
  public void onRefundCompleted(RefundCompletedEvent event) {
    ChoreographySagaInstance instance = requireInstance(event.getSagaId());
    instance.setStep("REFUND", "COMPLETED");

    int pending = instance.decrementCompensationsPending();
    if (pending == 0 && instance.getStatus() != ChoreographySagaStatus.COMPLETED) {
      instance.setStatus(ChoreographySagaStatus.FAILED);
    }
  }

  @KafkaListener(topics = ChoreographyTopicNames.INVENTORY_RELEASED_EVENT, groupId = "choreography-order-service")
  public void onInventoryReleased(InventoryReleasedEvent event) {
    ChoreographySagaInstance instance = requireInstance(event.getSagaId());
    instance.setStep("INVENTORY_RELEASE", "COMPLETED");

    int pending = instance.decrementCompensationsPending();
    if (pending == 0 && instance.getStatus() != ChoreographySagaStatus.COMPLETED) {
      instance.setStatus(ChoreographySagaStatus.FAILED);
    }
  }

  private ChoreographySagaInstance requireInstance(UUID sagaId) {
    ChoreographySagaInstance instance = store.get(sagaId);
    if (instance == null) {
      throw new IllegalStateException("Unknown sagaId " + sagaId);
    }
    return instance;
  }
}

