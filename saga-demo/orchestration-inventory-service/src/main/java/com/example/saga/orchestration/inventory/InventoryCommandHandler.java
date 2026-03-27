package com.example.saga.orchestration.inventory;

import com.example.saga.shared.contracts.FailAt;
import com.example.saga.shared.contracts.orchestration.*;
import com.example.saga.shared.contracts.topic.OrchestrationTopicNames;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryCommandHandler {
  private final KafkaTemplate<String, Object> kafkaTemplate;

  public InventoryCommandHandler(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @KafkaListener(
      topics = OrchestrationTopicNames.INVENTORY_RESERVE_COMMAND,
      groupId = "orchestration-inventory-service"
  )
  public void onReserveInventory(ReserveInventoryCommand command) {
    boolean shouldFail = command.getFailAt() == FailAt.INVENTORY;

    if (shouldFail) {
      InventoryReserveFailedEvent failed = new InventoryReserveFailedEvent(
          command.getSagaId(),
          command.getOrderId(),
          command.getCorrelationId(),
          "Simulated INVENTORY reservation failure",
          command.getFailAt()
      );
      kafkaTemplate.send(OrchestrationTopicNames.INVENTORY_FAILED_EVENT, command.getSagaId().toString(), failed);
      return;
    }

    InventoryReservedEvent reserved = new InventoryReservedEvent(
        command.getSagaId(),
        command.getOrderId(),
        command.getCorrelationId(),
        command.getFailAt()
    );
    kafkaTemplate.send(OrchestrationTopicNames.INVENTORY_RESERVED_EVENT, command.getSagaId().toString(), reserved);
  }

  @KafkaListener(
      topics = OrchestrationTopicNames.INVENTORY_RELEASE_COMMAND,
      groupId = "orchestration-inventory-service"
  )
  public void onReleaseInventory(ReleaseInventoryCommand command) {
    InventoryReleasedEvent released = new InventoryReleasedEvent(
        command.getSagaId(),
        command.getOrderId(),
        command.getCorrelationId(),
        command.getFailAt()
    );
    kafkaTemplate.send(OrchestrationTopicNames.INVENTORY_RELEASED_EVENT, command.getSagaId().toString(), released);
  }
}

