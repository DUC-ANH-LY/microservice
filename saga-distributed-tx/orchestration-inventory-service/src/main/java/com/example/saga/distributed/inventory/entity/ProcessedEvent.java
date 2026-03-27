package com.example.saga.distributed.inventory.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events", uniqueConstraints = @UniqueConstraint(columnNames = "message_id"))
public class ProcessedEvent {

  @Id
  @Column(name = "message_id")
  private UUID messageId;

  @Column(name = "processed_at", nullable = false)
  private Instant processedAt;

  protected ProcessedEvent() {
  }

  public ProcessedEvent(UUID messageId) {
    this.messageId = messageId;
    this.processedAt = Instant.now();
  }

  public UUID getMessageId() {
    return messageId;
  }

  public Instant getProcessedAt() {
    return processedAt;
  }
}
