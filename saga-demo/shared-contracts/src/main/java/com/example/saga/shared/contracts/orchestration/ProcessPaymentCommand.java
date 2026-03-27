package com.example.saga.shared.contracts.orchestration;

import com.example.saga.shared.contracts.FailAt;

import java.util.UUID;

public class ProcessPaymentCommand {
  private UUID sagaId;
  private String orderId;
  private UUID correlationId;
  private FailAt failAt;

  public ProcessPaymentCommand() {
  }

  public ProcessPaymentCommand(UUID sagaId, String orderId, UUID correlationId, FailAt failAt) {
    this.sagaId = sagaId;
    this.orderId = orderId;
    this.correlationId = correlationId;
    this.failAt = failAt;
  }

  public UUID getSagaId() {
    return sagaId;
  }

  public void setSagaId(UUID sagaId) {
    this.sagaId = sagaId;
  }

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public UUID getCorrelationId() {
    return correlationId;
  }

  public void setCorrelationId(UUID correlationId) {
    this.correlationId = correlationId;
  }

  public FailAt getFailAt() {
    return failAt;
  }

  public void setFailAt(FailAt failAt) {
    this.failAt = failAt;
  }
}

