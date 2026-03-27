package com.example.saga.twophase.shipping.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prepared_tx", uniqueConstraints = @UniqueConstraint(columnNames = "transaction_id"))
public class PreparedShipment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "transaction_id", nullable = false, unique = true)
  private UUID transactionId;

  @Column(name = "order_id", nullable = false)
  private String orderId;

  @Column(nullable = false)
  private String address;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected PreparedShipment() {}

  public PreparedShipment(UUID transactionId, String orderId, String address) {
    this.transactionId = transactionId;
    this.orderId = orderId;
    this.address = address;
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

  public String getAddress() {
    return address;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
