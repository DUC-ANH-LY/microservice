package com.example.saga.distributed.orchestrator.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_message", uniqueConstraints = @UniqueConstraint(columnNames = {"saga_id", "message_id"}))
public class ProcessedMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "saga_id", nullable = false)
  private UUID sagaId;

  @Column(name = "message_id", nullable = false)
  private UUID messageId;

  @Column(name = "processed_at", nullable = false)
  private Instant processedAt;

  protected ProcessedMessage() {
  }

  public ProcessedMessage(UUID sagaId, UUID messageId) {
    this.sagaId = sagaId;
    this.messageId = messageId;
    this.processedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public UUID getSagaId() {
    return sagaId;
  }

  public UUID getMessageId() {
    return messageId;
  }

  public Instant getProcessedAt() {
    return processedAt;
  }
}
