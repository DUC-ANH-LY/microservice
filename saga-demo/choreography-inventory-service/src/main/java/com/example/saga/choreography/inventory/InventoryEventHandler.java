package com.example.saga.choreography.inventory;

import com.example.saga.shared.contracts.FailAt;
import com.example.saga.shared.contracts.orchestration.*;
import com.example.saga.shared.contracts.topic.ChoreographyTopicNames;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryEventHandler {
  private final KafkaTemplate<String, Object> kafkaTemplate;

  public InventoryEventHandler(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @KafkaListener(topics = ChoreographyTopicNames.PAYMENT_SUCCEEDED_EVENT, groupId = "choreography-inventory-service")
  public void onPaymentSucceeded(PaymentSucceededEvent event) {
    boolean shouldFail = event.getFailAt() == FailAt.INVENTORY;

    if (shouldFail) {
      InventoryReserveFailedEvent failed = new InventoryReserveFailedEvent(
          event.getSagaId(),
          event.getOrderId(),
          event.getCorrelationId(),
          "Simulated INVENTORY reservation failure",
          event.getFailAt()
      );
      kafkaTemplate.send(ChoreographyTopicNames.INVENTORY_FAILED_EVENT, event.getSagaId().toString(), failed);

      // Compensation: refund requested to payment service.
      RefundCommand refundCommand = new RefundCommand(
          event.getSagaId(),
          event.getOrderId(),
          event.getCorrelationId(),
          event.getFailAt()
      );
      kafkaTemplate.send(ChoreographyTopicNames.REFUND_COMMAND, event.getSagaId().toString(), refundCommand);
      return;
    }

    InventoryReservedEvent reserved = new InventoryReservedEvent(
        event.getSagaId(),
        event.getOrderId(),
        event.getCorrelationId(),
        event.getFailAt()
    );
    kafkaTemplate.send(ChoreographyTopicNames.INVENTORY_RESERVED_EVENT, event.getSagaId().toString(), reserved);
  }

  @KafkaListener(topics = ChoreographyTopicNames.INVENTORY_RELEASE_COMMAND, groupId = "choreography-inventory-service")
  public void onReleaseInventory(ReleaseInventoryCommand command) {
    InventoryReleasedEvent released = new InventoryReleasedEvent(
        command.getSagaId(),
        command.getOrderId(),
        command.getCorrelationId(),
        command.getFailAt()
    );
    kafkaTemplate.send(ChoreographyTopicNames.INVENTORY_RELEASED_EVENT, command.getSagaId().toString(), released);
  }
}

