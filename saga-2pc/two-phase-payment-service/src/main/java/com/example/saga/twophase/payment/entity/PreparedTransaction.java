package com.example.saga.twophase.payment.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prepared_tx", uniqueConstraints = @UniqueConstraint(columnNames = "transaction_id"))
public class PreparedTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "transaction_id", nullable = false, unique = true)
  private UUID transactionId;

  @Column(name = "order_id", nullable = false)
  private String orderId;

  @Column(nullable = false)
  private int amount;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected PreparedTransaction() {}

  public PreparedTransaction(UUID transactionId, String orderId, int amount) {
    this.transactionId = transactionId;
    this.orderId = orderId;
    this.amount = amount;
    this.createdAt = Instant.now();
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

  public int getAmount() {
    return amount;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
