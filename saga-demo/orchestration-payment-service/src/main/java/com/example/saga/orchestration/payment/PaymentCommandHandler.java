package com.example.saga.orchestration.payment;

import com.example.saga.shared.contracts.FailAt;
import com.example.saga.shared.contracts.orchestration.*;
import com.example.saga.shared.contracts.topic.OrchestrationTopicNames;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentCommandHandler {
  private final KafkaTemplate<String, Object> kafkaTemplate;

  public PaymentCommandHandler(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @KafkaListener(
      topics = OrchestrationTopicNames.PAYMENT_PROCESS_COMMAND,
      groupId = "orchestration-payment-service"
  )
  public void onProcessPayment(ProcessPaymentCommand command) {
    boolean shouldFail = command.getFailAt() == FailAt.PAYMENT;

    if (shouldFail) {
      PaymentFailedEvent failed = new PaymentFailedEvent(
          command.getSagaId(),
          command.getOrderId(),
          command.getCorrelationId(),
          "Simulated PAYMENT failure",
          command.getFailAt()
      );
      kafkaTemplate.send(OrchestrationTopicNames.PAYMENT_FAILED_EVENT, command.getSagaId().toString(), failed);
      return;
    }

    PaymentSucceededEvent succeeded = new PaymentSucceededEvent(
        command.getSagaId(),
        command.getOrderId(),
        command.getCorrelationId(),
        command.getFailAt()
    );
    kafkaTemplate.send(OrchestrationTopicNames.PAYMENT_SUCCEEDED_EVENT, command.getSagaId().toString(), succeeded);
  }

  @KafkaListener(
      topics = OrchestrationTopicNames.PAYMENT_REFUND_COMMAND,
      groupId = "orchestration-payment-service"
  )
  public void onRefund(RefundCommand command) {
    // In this demo, compensation always succeeds.
    RefundCompletedEvent completed = new RefundCompletedEvent(
        command.getSagaId(),
        command.getOrderId(),
        command.getCorrelationId(),
        command.getFailAt()
    );
    kafkaTemplate.send(OrchestrationTopicNames.PAYMENT_REFUND_COMPLETED_EVENT, command.getSagaId().toString(), completed);
  }
}

