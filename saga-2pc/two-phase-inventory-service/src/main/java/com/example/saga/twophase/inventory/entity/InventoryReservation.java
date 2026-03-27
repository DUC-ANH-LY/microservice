package com.example.saga.twophase.inventory.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_reservation", uniqueConstraints = @UniqueConstraint(columnNames = "transaction_id"))
public class InventoryReservation {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "transaction_id", nullable = false, unique = true)
  private UUID transactionId;

  @Column(name = "order_id", nullable = false)
  private String orderId;

  @Column(nullable = false)
  private int quantity;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected InventoryReservation() {}

  public InventoryReservation(UUID transactionId, String orderId, int quantity) {
    this.transactionId = transactionId;
    this.orderId = orderId;
    this.quantity = quantity;
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

  public int getQuantity() {
    return quantity;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
