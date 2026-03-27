package com.example.saga.choreography.payment;

import com.example.saga.shared.contracts.FailAt;
import com.example.saga.shared.contracts.orchestration.*;
import com.example.saga.shared.contracts.topic.ChoreographyTopicNames;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventHandler {
  private final KafkaTemplate<String, Object> kafkaTemplate;

  public PaymentEventHandler(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @KafkaListener(topics = ChoreographyTopicNames.ORDER_CREATED_EVENT, groupId = "choreography-payment-service")
  public void onOrderCreated(OrderCreatedEvent event) {
    boolean shouldFail = event.getFailAt() == FailAt.PAYMENT;

    if (shouldFail) {
      PaymentFailedEvent failed = new PaymentFailedEvent(
          event.getSagaId(),
          event.getOrderId(),
          event.getCorrelationId(),
          "Simulated PAYMENT failure",
          event.getFailAt()
      );
      kafkaTemplate.send(ChoreographyTopicNames.PAYMENT_FAILED_EVENT, event.getSagaId().toString(), failed);
      return;
    }

    PaymentSucceededEvent succeeded = new PaymentSucceededEvent(
        event.getSagaId(),
        event.getOrderId(),
        event.getCorrelationId(),
        event.getFailAt()
    );
    kafkaTemplate.send(ChoreographyTopicNames.PAYMENT_SUCCEEDED_EVENT, event.getSagaId().toString(), succeeded);
  }

  @KafkaListener(topics = ChoreographyTopicNames.REFUND_COMMAND, groupId = "choreography-payment-service")
  public void onRefund(RefundCommand command) {
    RefundCompletedEvent completed = new RefundCompletedEvent(
        command.getSagaId(),
        command.getOrderId(),
        command.getCorrelationId(),
        command.getFailAt()
    );
    kafkaTemplate.send(ChoreographyTopicNames.REFUND_COMPLETED_EVENT, command.getSagaId().toString(), completed);
  }
}

