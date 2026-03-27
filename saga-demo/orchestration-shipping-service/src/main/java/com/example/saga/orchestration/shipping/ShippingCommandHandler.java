package com.example.saga.orchestration.shipping;

import com.example.saga.shared.contracts.FailAt;
import com.example.saga.shared.contracts.orchestration.*;
import com.example.saga.shared.contracts.topic.OrchestrationTopicNames;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ShippingCommandHandler {
  private final KafkaTemplate<String, Object> kafkaTemplate;

  public ShippingCommandHandler(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @KafkaListener(
      topics = OrchestrationTopicNames.SHIPPING_SHIP_COMMAND,
      groupId = "orchestration-shipping-service"
  )
  public void onShip(ShipOrderCommand command) {
    boolean shouldFail = command.getFailAt() == FailAt.SHIPMENT;

    if (shouldFail) {
      ShipmentFailedEvent failed = new ShipmentFailedEvent(
          command.getSagaId(),
          command.getOrderId(),
          command.getCorrelationId(),
          "Simulated SHIPMENT failure",
          command.getFailAt()
      );
      kafkaTemplate.send(OrchestrationTopicNames.SHIPMENT_FAILED_EVENT, command.getSagaId().toString(), failed);
      return;
    }

    ShipmentShippedEvent shipped = new ShipmentShippedEvent(
        command.getSagaId(),
        command.getOrderId(),
        command.getCorrelationId(),
        command.getFailAt()
    );
    kafkaTemplate.send(OrchestrationTopicNames.SHIPMENT_SHIPPED_EVENT, command.getSagaId().toString(), shipped);
  }
}

