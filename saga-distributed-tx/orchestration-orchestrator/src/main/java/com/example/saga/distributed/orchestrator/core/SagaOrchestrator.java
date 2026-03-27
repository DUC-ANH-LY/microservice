package com.example.saga.distributed.orchestrator.core;

import com.example.saga.distributed.contracts.FailAt;
import com.example.saga.distributed.contracts.SagaStartRequest;
import com.example.saga.distributed.contracts.orchestration.*;
import com.example.saga.distributed.contracts.topic.DistributedTopicNames;
import com.example.saga.distributed.orchestrator.api.SagaController;
import com.example.saga.distributed.orchestrator.entity.OutboxMessage;
import com.example.saga.distributed.orchestrator.entity.ProcessedMessage;
import com.example.saga.distributed.orchestrator.entity.SagaInstance;
import com.example.saga.distributed.orchestrator.repository.OutboxRepository;
import com.example.saga.distributed.orchestrator.repository.ProcessedMessageRepository;
import com.example.saga.distributed.orchestrator.repository.SagaInstanceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SagaOrchestrator {

  private final SagaInstanceRepository sagaRepository;
  private final OutboxRepository outboxRepository;
  private final ProcessedMessageRepository processedMessageRepository;
  private final ObjectMapper objectMapper;

  public SagaOrchestrator(SagaInstanceRepository sagaRepository,
                          OutboxRepository outboxRepository,
                          ProcessedMessageRepository processedMessageRepository,
                          ObjectMapper objectMapper) {
    this.sagaRepository = sagaRepository;
    this.outboxRepository = outboxRepository;
    this.processedMessageRepository = processedMessageRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public UUID startSaga(SagaStartRequest request) {
    UUID sagaId = UUID.randomUUID();
    UUID correlationId = UUID.randomUUID();
    FailAt failAt = request.getFailAt() != null ? request.getFailAt() : FailAt.NONE;

    SagaInstance instance = new SagaInstance(sagaId, request.getOrderId(), correlationId, failAt.name());
    instance.setStep("PAYMENT", "PENDING");
    instance.setStep("INVENTORY", "PENDING");
    instance.setStep("SHIPMENT", "PENDING");
    sagaRepository.save(instance);

    UUID msgId = UUID.randomUUID();
    ProcessPaymentCommand cmd = new ProcessPaymentCommand(msgId, sagaId, request.getOrderId(), correlationId, failAt);
    String payload = toJson(cmd);
    OutboxMessage outbox = new OutboxMessage(sagaId.toString(), DistributedTopicNames.PAYMENT_PROCESS_COMMAND, sagaId.toString(), payload);
    outboxRepository.save(outbox);

    return sagaId;
  }

  public SagaController.SagaStatusResponse getStatus(UUID sagaId) {
    return sagaRepository.findBySagaId(sagaId)
        .map(inst -> {
          List<SagaController.StepStatus> steps = new ArrayList<>();
          for (Map.Entry<String, String> e : inst.getSteps().entrySet()) {
            steps.add(new SagaController.StepStatus(e.getKey(), e.getValue()));
          }
          return new SagaController.SagaStatusResponse(inst.getSagaId(), inst.getStatus().name(), steps);
        })
        .orElse(new SagaController.SagaStatusResponse(sagaId, SagaInstance.SagaStatus.FAILED.name(), List.of()));
  }

  @KafkaListener(topics = DistributedTopicNames.PAYMENT_SUCCEEDED_EVENT, groupId = "saga-distributed-orchestrator")
  @Transactional
  public void onPaymentSucceeded(PaymentSucceededEvent event) {
    if (processedMessageRepository.findBySagaIdAndMessageId(event.getSagaId(), event.getMessageId()).isPresent()) {
      return;
    }

    SagaInstance instance = sagaRepository.findBySagaId(event.getSagaId())
        .orElseThrow(() -> new IllegalStateException("Unknown saga " + event.getSagaId()));

    processedMessageRepository.save(new ProcessedMessage(event.getSagaId(), event.getMessageId()));
    instance.setStep("PAYMENT", "SUCCEEDED");
    instance.setStep("INVENTORY", "PENDING");

    UUID msgId = UUID.randomUUID();
    ReserveInventoryCommand cmd = new ReserveInventoryCommand(msgId, event.getSagaId(), event.getOrderId(), event.getCorrelationId(), event.getFailAt());
    outboxRepository.save(new OutboxMessage(event.getSagaId().toString(), DistributedTopicNames.INVENTORY_RESERVE_COMMAND, event.getSagaId().toString(), toJson(cmd)));
  }

  @KafkaListener(topics = DistributedTopicNames.PAYMENT_FAILED_EVENT, groupId = "saga-distributed-orchestrator")
  @Transactional
  public void onPaymentFailed(PaymentFailedEvent event) {
    if (processedMessageRepository.findBySagaIdAndMessageId(event.getSagaId(), event.getMessageId()).isPresent()) {
      return;
    }

    SagaInstance instance = sagaRepository.findBySagaId(event.getSagaId())
        .orElseThrow(() -> new IllegalStateException("Unknown saga " + event.getSagaId()));

    processedMessageRepository.save(new ProcessedMessage(event.getSagaId(), event.getMessageId()));
    instance.setStep("PAYMENT", "FAILED");
    instance.setStatus(SagaInstance.SagaStatus.FAILED);
  }

  @KafkaListener(topics = DistributedTopicNames.INVENTORY_RESERVED_EVENT, groupId = "saga-distributed-orchestrator")
  @Transactional
  public void onInventoryReserved(InventoryReservedEvent event) {
    if (processedMessageRepository.findBySagaIdAndMessageId(event.getSagaId(), event.getMessageId()).isPresent()) {
      return;
    }

    SagaInstance instance = sagaRepository.findBySagaId(event.getSagaId())
        .orElseThrow(() -> new IllegalStateException("Unknown saga " + event.getSagaId()));

    processedMessageRepository.save(new ProcessedMessage(event.getSagaId(), event.getMessageId()));
    instance.setStep("INVENTORY", "SUCCEEDED");
    instance.setStep("SHIPMENT", "PENDING");

    UUID msgId = UUID.randomUUID();
    ShipOrderCommand cmd = new ShipOrderCommand(msgId, event.getSagaId(), event.getOrderId(), event.getCorrelationId(), event.getFailAt());
    outboxRepository.save(new OutboxMessage(event.getSagaId().toString(), DistributedTopicNames.SHIPPING_SHIP_COMMAND, event.getSagaId().toString(), toJson(cmd)));
  }

  @KafkaListener(topics = DistributedTopicNames.INVENTORY_FAILED_EVENT, groupId = "saga-distributed-orchestrator")
  @Transactional
  public void onInventoryFailed(InventoryReserveFailedEvent event) {
    if (processedMessageRepository.findBySagaIdAndMessageId(event.getSagaId(), event.getMessageId()).isPresent()) {
      return;
    }

    SagaInstance instance = sagaRepository.findBySagaId(event.getSagaId())
        .orElseThrow(() -> new IllegalStateException("Unknown saga " + event.getSagaId()));

    processedMessageRepository.save(new ProcessedMessage(event.getSagaId(), event.getMessageId()));
    instance.setStep("INVENTORY", "FAILED");
    instance.setStep("REFUND", "IN_PROGRESS");
    instance.setCompensationsPending(1);

    UUID msgId = UUID.randomUUID();
    RefundCommand cmd = new RefundCommand(msgId, event.getSagaId(), event.getOrderId(), event.getCorrelationId(), event.getFailAt());
    outboxRepository.save(new OutboxMessage(event.getSagaId().toString(), DistributedTopicNames.PAYMENT_REFUND_COMMAND, event.getSagaId().toString(), toJson(cmd)));
  }

  @KafkaListener(topics = DistributedTopicNames.SHIPMENT_SHIPPED_EVENT, groupId = "saga-distributed-orchestrator")
  @Transactional
  public void onShipmentShipped(ShipmentShippedEvent event) {
    if (processedMessageRepository.findBySagaIdAndMessageId(event.getSagaId(), event.getMessageId()).isPresent()) {
      return;
    }

    SagaInstance instance = sagaRepository.findBySagaId(event.getSagaId())
        .orElseThrow(() -> new IllegalStateException("Unknown saga " + event.getSagaId()));

    processedMessageRepository.save(new ProcessedMessage(event.getSagaId(), event.getMessageId()));
    instance.setStep("SHIPMENT", "SUCCEEDED");
    instance.setStatus(SagaInstance.SagaStatus.COMPLETED);
  }

  @KafkaListener(topics = DistributedTopicNames.SHIPMENT_FAILED_EVENT, groupId = "saga-distributed-orchestrator")
  @Transactional
  public void onShipmentFailed(ShipmentFailedEvent event) {
    if (processedMessageRepository.findBySagaIdAndMessageId(event.getSagaId(), event.getMessageId()).isPresent()) {
      return;
    }

    SagaInstance instance = sagaRepository.findBySagaId(event.getSagaId())
        .orElseThrow(() -> new IllegalStateException("Unknown saga " + event.getSagaId()));

    processedMessageRepository.save(new ProcessedMessage(event.getSagaId(), event.getMessageId()));
    instance.setStep("SHIPMENT", "FAILED");
    instance.setStep("INVENTORY_RELEASE", "IN_PROGRESS");
    instance.setStep("REFUND", "IN_PROGRESS");
    instance.setCompensationsPending(2);

    UUID msgId1 = UUID.randomUUID();
    UUID msgId2 = UUID.randomUUID();
    ReleaseInventoryCommand releaseCmd = new ReleaseInventoryCommand(msgId1, event.getSagaId(), event.getOrderId(), event.getCorrelationId(), event.getFailAt());
    RefundCommand refundCmd = new RefundCommand(msgId2, event.getSagaId(), event.getOrderId(), event.getCorrelationId(), event.getFailAt());
    outboxRepository.save(new OutboxMessage(event.getSagaId().toString(), DistributedTopicNames.INVENTORY_RELEASE_COMMAND, event.getSagaId().toString(), toJson(releaseCmd)));
    outboxRepository.save(new OutboxMessage(event.getSagaId().toString(), DistributedTopicNames.PAYMENT_REFUND_COMMAND, event.getSagaId().toString(), toJson(refundCmd)));
  }

  @KafkaListener(topics = DistributedTopicNames.PAYMENT_REFUND_COMPLETED_EVENT, groupId = "saga-distributed-orchestrator")
  @Transactional
  public void onRefundCompleted(RefundCompletedEvent event) {
    if (processedMessageRepository.findBySagaIdAndMessageId(event.getSagaId(), event.getMessageId()).isPresent()) {
      return;
    }

    SagaInstance instance = sagaRepository.findBySagaId(event.getSagaId())
        .orElseThrow(() -> new IllegalStateException("Unknown saga " + event.getSagaId()));

    processedMessageRepository.save(new ProcessedMessage(event.getSagaId(), event.getMessageId()));
    instance.setStep("REFUND", "COMPLETED");

    int pending = instance.decrementCompensationsPending();
    if (pending == 0 && instance.getStatus() != SagaInstance.SagaStatus.COMPLETED) {
      instance.setStatus(SagaInstance.SagaStatus.FAILED);
    }
  }

  @KafkaListener(topics = DistributedTopicNames.INVENTORY_RELEASED_EVENT, groupId = "saga-distributed-orchestrator")
  @Transactional
  public void onInventoryReleased(InventoryReleasedEvent event) {
    if (processedMessageRepository.findBySagaIdAndMessageId(event.getSagaId(), event.getMessageId()).isPresent()) {
      return;
    }

    SagaInstance instance = sagaRepository.findBySagaId(event.getSagaId())
        .orElseThrow(() -> new IllegalStateException("Unknown saga " + event.getSagaId()));

    processedMessageRepository.save(new ProcessedMessage(event.getSagaId(), event.getMessageId()));
    instance.setStep("INVENTORY_RELEASE", "COMPLETED");

    int pending = instance.decrementCompensationsPending();
    if (pending == 0 && instance.getStatus() != SagaInstance.SagaStatus.COMPLETED) {
      instance.setStatus(SagaInstance.SagaStatus.FAILED);
    }
  }

  private String toJson(Object o) {
    try {
      return objectMapper.writeValueAsString(o);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
