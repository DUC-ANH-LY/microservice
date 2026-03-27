package com.example.saga.choreography.shipping;

import com.example.saga.shared.contracts.FailAt;
import com.example.saga.shared.contracts.orchestration.*;
import com.example.saga.shared.contracts.topic.ChoreographyTopicNames;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ShippingEventHandler {
  private final KafkaTemplate<String, Object> kafkaTemplate;

  public ShippingEventHandler(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @KafkaListener(topics = ChoreographyTopicNames.INVENTORY_RESERVED_EVENT, groupId = "choreography-shipping-service")
  public void onInventoryReserved(InventoryReservedEvent event) {
    boolean shouldFail = event.getFailAt() == FailAt.SHIPMENT;

    if (shouldFail) {
      ShipmentFailedEvent failed = new ShipmentFailedEvent(
          event.getSagaId(),
          event.getOrderId(),
          event.getCorrelationId(),
          "Simulated SHIPMENT failure",
          event.getFailAt()
      );
      kafkaTemplate.send(ChoreographyTopicNames.SHIPMENT_FAILED_EVENT, event.getSagaId().toString(), failed);

      // Compensation: release inventory and refund payment.
      ReleaseInventoryCommand releaseCommand = new ReleaseInventoryCommand(
          event.getSagaId(),
          event.getOrderId(),
          event.getCorrelationId(),
          event.getFailAt()
      );
      RefundCommand refundCommand = new RefundCommand(
          event.getSagaId(),
          event.getOrderId(),
          event.getCorrelationId(),
          event.getFailAt()
      );

      kafkaTemplate.send(ChoreographyTopicNames.INVENTORY_RELEASE_COMMAND, event.getSagaId().toString(), releaseCommand);
      kafkaTemplate.send(ChoreographyTopicNames.REFUND_COMMAND, event.getSagaId().toString(), refundCommand);
      return;
    }

    ShipmentShippedEvent shipped = new ShipmentShippedEvent(
        event.getSagaId(),
        event.getOrderId(),
        event.getCorrelationId(),
        event.getFailAt()
    );
    kafkaTemplate.send(ChoreographyTopicNames.SHIPMENT_SHIPPED_EVENT, event.getSagaId().toString(), shipped);
  }
}

