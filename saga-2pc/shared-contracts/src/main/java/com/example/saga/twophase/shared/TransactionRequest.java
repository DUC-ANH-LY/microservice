package com.example.saga.twophase.shared;

/**
 * Initial request to start a 2PC transaction.
 */
public record TransactionRequest(
    String orderId,
    int amount,
    int quantity,
    String address,
    FailAt failAt
) {
  public TransactionRequest(String orderId, int amount, int quantity, String address, FailAt failAt) {
    if (orderId == null || orderId.isBlank()) {
      throw new IllegalArgumentException("orderId required");
    }
    if (amount <= 0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    if (quantity <= 0) {
      throw new IllegalArgumentException("quantity must be positive");
    }
    if (address == null || address.isBlank()) {
      throw new IllegalArgumentException("address required");
    }
    this.orderId = orderId;
    this.amount = amount;
    this.quantity = quantity;
    this.address = address;
    this.failAt = failAt != null ? failAt : FailAt.NONE;
  }
}
