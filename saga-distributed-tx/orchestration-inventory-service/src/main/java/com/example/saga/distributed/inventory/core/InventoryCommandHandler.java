package com.example.saga.distributed.inventory.core;

import com.example.saga.distributed.contracts.FailAt;
import com.example.saga.distributed.contracts.orchestration.*;
import com.example.saga.distributed.contracts.topic.DistributedTopicNames;
import com.example.saga.distributed.inventory.entity.OutboxMessage;
import com.example.saga.distributed.inventory.entity.ProcessedEvent;
import com.example.saga.distributed.inventory.repository.OutboxRepository;
import com.example.saga.distributed.inventory.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InventoryCommandHandler {

  private final ProcessedEventRepository processedEventRepository;
  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  public InventoryCommandHandler(ProcessedEventRepository processedEventRepository,
                                 OutboxRepository outboxRepository,
                                 ObjectMapper objectMapper) {
    this.processedEventRepository = processedEventRepository;
    this.outboxRepository = outboxRepository;
    this.objectMapper = objectMapper;
  }

  @KafkaListener(topics = DistributedTopicNames.INVENTORY_RESERVE_COMMAND, groupId = "saga-distributed-inventory")
  @Transactional
  public void onReserveInventory(ReserveInventoryCommand command) {
    if (processedEventRepository.existsByMessageId(command.getMessageId())) {
      return;
    }

    processedEventRepository.save(new ProcessedEvent(command.getMessageId()));

    boolean shouldFail = command.getFailAt() == FailAt.INVENTORY;

    if (shouldFail) {
      UUID msgId = UUID.randomUUID();
      InventoryReserveFailedEvent failed = new InventoryReserveFailedEvent(
          msgId,
          command.getSagaId(),
          command.getOrderId(),
          command.getCorrelationId(),
          "Simulated INVENTORY reservation failure",
          command.getFailAt()
      );
      outboxRepository.save(new OutboxMessage(
          DistributedTopicNames.INVENTORY_FAILED_EVENT,
          command.getSagaId().toString(),
          toJson(failed)
      ));
    } else {
      UUID msgId = UUID.randomUUID();
      InventoryReservedEvent reserved = new InventoryReservedEvent(
          msgId,
          command.getSagaId(),
          command.getOrderId(),
          command.getCorrelationId(),
          command.getFailAt()
      );
      outboxRepository.save(new OutboxMessage(
          DistributedTopicNames.INVENTORY_RESERVED_EVENT,
          command.getSagaId().toString(),
          toJson(reserved)
      ));
    }
  }

  @KafkaListener(topics = DistributedTopicNames.INVENTORY_RELEASE_COMMAND, groupId = "saga-distributed-inventory")
  @Transactional
  public void onReleaseInventory(ReleaseInventoryCommand command) {
    if (processedEventRepository.existsByMessageId(command.getMessageId())) {
      return;
    }

    processedEventRepository.save(new ProcessedEvent(command.getMessageId()));

    UUID msgId = UUID.randomUUID();
    InventoryReleasedEvent released = new InventoryReleasedEvent(
        msgId,
        command.getSagaId(),
        command.getOrderId(),
        command.getCorrelationId(),
        command.getFailAt()
    );
    outboxRepository.save(new OutboxMessage(
        DistributedTopicNames.INVENTORY_RELEASED_EVENT,
        command.getSagaId().toString(),
        toJson(released)
    ));
  }

  private String toJson(Object o) {
    try {
      return objectMapper.writeValueAsString(o);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
