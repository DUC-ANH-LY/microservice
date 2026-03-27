package com.example.saga.shared.contracts.orchestration;

import com.example.saga.shared.contracts.FailAt;

import java.util.UUID;

public class InventoryReserveFailedEvent {
  private UUID sagaId;
  private String orderId;
  private UUID correlationId;
  private String error;
  private FailAt failAt;

  public InventoryReserveFailedEvent() {
  }

  public InventoryReserveFailedEvent(UUID sagaId, String orderId, UUID correlationId, String error) {
    this(sagaId, orderId, correlationId, error, null);
  }

  public InventoryReserveFailedEvent(UUID sagaId, String orderId, UUID correlationId, String error, FailAt failAt) {
    this.sagaId = sagaId;
    this.orderId = orderId;
    this.correlationId = correlationId;
    this.error = error;
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

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public FailAt getFailAt() {
    return failAt;
  }

  public void setFailAt(FailAt failAt) {
    this.failAt = failAt;
  }
}

