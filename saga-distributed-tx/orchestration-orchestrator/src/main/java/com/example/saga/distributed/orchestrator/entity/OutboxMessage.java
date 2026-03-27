package com.example.saga.distributed.orchestrator.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox")
public class OutboxMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "saga_id")
  private String sagaId;

  @Column(nullable = false)
  private String topic;

  @Column(name = "msg_key")
  private String key;

  @Column(name = "payload_json", columnDefinition = "CLOB", nullable = false)
  private String payload;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "sent_at")
  private Instant sentAt;

  protected OutboxMessage() {
  }

  public OutboxMessage(String sagaId, String topic, String key, String payload) {
    this.sagaId = sagaId;
    this.topic = topic;
    this.key = key;
    this.payload = payload;
    this.createdAt = Instant.now();
    this.sentAt = null;
  }

  public UUID getId() {
    return id;
  }

  public String getSagaId() {
    return sagaId;
  }

  public String getTopic() {
    return topic;
  }

  public String getKey() {
    return key;
  }

  public String getPayload() {
    return payload;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getSentAt() {
    return sentAt;
  }

  public void setSentAt(Instant sentAt) {
    this.sentAt = sentAt;
  }
}
