package com.example.saga.twophase.coordinator.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transaction_log")
public class TransactionLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "transaction_id", nullable = false, unique = true)
  private UUID transactionId;

  @Column(name = "order_id", nullable = false)
  private String orderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransactionStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public enum TransactionStatus {
    PREPARING,
    COMMITTED,
    ABORTED
  }

  protected TransactionLog() {}

  public TransactionLog(UUID transactionId, String orderId, TransactionStatus status) {
    this.transactionId = transactionId;
    this.orderId = orderId;
    this.status = status;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public UUID getTransactionId() {
    return transactionId;
  }

  public String getOrderId() {
    return orderId;
  }

  public TransactionStatus getStatus() {
    return status;
  }

  public void setStatus(TransactionStatus status) {
    this.status = status;
    this.updatedAt = Instant.now();
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
