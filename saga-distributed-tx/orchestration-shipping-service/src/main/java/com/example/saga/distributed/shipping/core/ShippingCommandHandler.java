package com.example.saga.distributed.shipping.core;

import com.example.saga.distributed.contracts.FailAt;
import com.example.saga.distributed.contracts.orchestration.*;
import com.example.saga.distributed.contracts.topic.DistributedTopicNames;
import com.example.saga.distributed.shipping.entity.OutboxMessage;
import com.example.saga.distributed.shipping.entity.ProcessedEvent;
import com.example.saga.distributed.shipping.repository.OutboxRepository;
import com.example.saga.distributed.shipping.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ShippingCommandHandler {

  private final ProcessedEventRepository processedEventRepository;
  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  public ShippingCommandHandler(ProcessedEventRepository processedEventRepository,
                                OutboxRepository outboxRepository,
                                ObjectMapper objectMapper) {
    this.processedEventRepository = processedEventRepository;
    this.outboxRepository = outboxRepository;
    this.objectMapper = objectMapper;
  }

  @KafkaListener(topics = DistributedTopicNames.SHIPPING_SHIP_COMMAND, groupId = "saga-distributed-shipping")
  @Transactional
  public void onShip(ShipOrderCommand command) {
    if (processedEventRepository.existsByMessageId(command.getMessageId())) {
      return;
    }

    processedEventRepository.save(new ProcessedEvent(command.getMessageId()));

    boolean shouldFail = command.getFailAt() == FailAt.SHIPMENT;

    if (shouldFail) {
      UUID msgId = UUID.randomUUID();
      ShipmentFailedEvent failed = new ShipmentFailedEvent(
          msgId,
          command.getSagaId(),
          command.getOrderId(),
          command.getCorrelationId(),
          "Simulated SHIPMENT failure",
          command.getFailAt()
      );
      outboxRepository.save(new OutboxMessage(
          DistributedTopicNames.SHIPMENT_FAILED_EVENT,
          command.getSagaId().toString(),
          toJson(failed)
      ));
    } else {
      UUID msgId = UUID.randomUUID();
      ShipmentShippedEvent shipped = new ShipmentShippedEvent(
          msgId,
          command.getSagaId(),
          command.getOrderId(),
          command.getCorrelationId(),
          command.getFailAt()
      );
      outboxRepository.save(new OutboxMessage(
          DistributedTopicNames.SHIPMENT_SHIPPED_EVENT,
          command.getSagaId().toString(),
          toJson(shipped)
      ));
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
