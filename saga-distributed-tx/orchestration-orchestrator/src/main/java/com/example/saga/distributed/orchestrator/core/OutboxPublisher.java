package com.example.saga.distributed.orchestrator.core;

import com.example.saga.distributed.orchestrator.entity.OutboxMessage;
import com.example.saga.distributed.orchestrator.repository.OutboxRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxPublisher {

  private final OutboxRepository outboxRepository;
  private final KafkaTemplate<String, String> kafkaTemplate;

  public OutboxPublisher(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
    this.outboxRepository = outboxRepository;
    this.kafkaTemplate = kafkaTemplate;
  }

  @Scheduled(fixedDelay = 500)
  public void publishUnsent() {
    List<OutboxMessage> unsent = outboxRepository.findUnsentOrderByCreatedAt(PageRequest.of(0, 100));
    for (OutboxMessage msg : unsent) {
      try {
        kafkaTemplate.send(msg.getTopic(), msg.getKey(), msg.getPayload());
        markSent(msg.getId());
      } catch (Exception e) {
        // Retry on next poll
      }
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markSent(java.util.UUID id) {
    outboxRepository.findById(id).ifPresent(m -> {
      m.setSentAt(Instant.now());
      outboxRepository.save(m);
    });
  }
}
