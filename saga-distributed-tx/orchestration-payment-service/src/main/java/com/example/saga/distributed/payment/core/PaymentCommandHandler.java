package com.example.saga.distributed.payment.core;

import com.example.saga.distributed.contracts.FailAt;
import com.example.saga.distributed.contracts.orchestration.*;
import com.example.saga.distributed.contracts.topic.DistributedTopicNames;
import com.example.saga.distributed.payment.entity.OutboxMessage;
import com.example.saga.distributed.payment.entity.ProcessedEvent;
import com.example.saga.distributed.payment.repository.OutboxRepository;
import com.example.saga.distributed.payment.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentCommandHandler {

  private final ProcessedEventRepository processedEventRepository;
  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  public PaymentCommandHandler(ProcessedEventRepository processedEventRepository,
                               OutboxRepository outboxRepository,
                               ObjectMapper objectMapper) {
    this.processedEventRepository = processedEventRepository;
    this.outboxRepository = outboxRepository;
    this.objectMapper = objectMapper;
  }

  @KafkaListener(topics = DistributedTopicNames.PAYMENT_PROCESS_COMMAND, groupId = "saga-distributed-payment")
  @Transactional
  public void onProcessPayment(ProcessPaymentCommand command) {
    if (processedEventRepository.existsByMessageId(command.getMessageId())) {
      return;
    }

    processedEventRepository.save(new ProcessedEvent(command.getMessageId()));

    boolean shouldFail = command.getFailAt() == FailAt.PAYMENT;

    if (shouldFail) {
      UUID msgId = UUID.randomUUID();
      PaymentFailedEvent failed = new PaymentFailedEvent(
          msgId,
          command.getSagaId(),
          command.getOrderId(),
          command.getCorrelationId(),
          "Simulated PAYMENT failure",
          command.getFailAt()
      );
      outboxRepository.save(new OutboxMessage(
          DistributedTopicNames.PAYMENT_FAILED_EVENT,
          command.getSagaId().toString(),
          toJson(failed)
      ));
    } else {
      UUID msgId = UUID.randomUUID();
      PaymentSucceededEvent succeeded = new PaymentSucceededEvent(
          msgId,
          command.getSagaId(),
          command.getOrderId(),
          command.getCorrelationId(),
          command.getFailAt()
      );
      outboxRepository.save(new OutboxMessage(
          DistributedTopicNames.PAYMENT_SUCCEEDED_EVENT,
          command.getSagaId().toString(),
          toJson(succeeded)
      ));
    }
  }

  @KafkaListener(topics = DistributedTopicNames.PAYMENT_REFUND_COMMAND, groupId = "saga-distributed-payment")
  @Transactional
  public void onRefund(RefundCommand command) {
    if (processedEventRepository.existsByMessageId(command.getMessageId())) {
      return;
    }

    processedEventRepository.save(new ProcessedEvent(command.getMessageId()));

    UUID msgId = UUID.randomUUID();
    RefundCompletedEvent completed = new RefundCompletedEvent(
        msgId,
        command.getSagaId(),
        command.getOrderId(),
        command.getCorrelationId(),
        command.getFailAt()
    );
    outboxRepository.save(new OutboxMessage(
        DistributedTopicNames.PAYMENT_REFUND_COMPLETED_EVENT,
        command.getSagaId().toString(),
        toJson(completed)
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
