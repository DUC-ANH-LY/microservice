package com.example.saga.twophase.shared;

import java.util.UUID;

/**
 * Request for a participant to prepare (Phase 1).
 */
public record PrepareRequest(
    UUID transactionId,
    String orderId,
    int amount,
    int quantity,
    String address,
    FailAt failAt
) {
  public PrepareRequest(UUID transactionId, String orderId, int amount, int quantity, String address, FailAt failAt) {
    if (transactionId == null) {
      throw new IllegalArgumentException("transactionId required");
    }
    if (orderId == null || orderId.isBlank()) {
      throw new IllegalArgumentException("orderId required");
    }
    this.transactionId = transactionId;
    this.orderId = orderId;
    this.amount = amount;
    this.quantity = quantity;
    this.address = address;
    this.failAt = failAt != null ? failAt : FailAt.NONE;
  }
}
