package com.example.saga.distributed.contracts.orchestration;

import com.example.saga.distributed.contracts.FailAt;

import java.util.UUID;

public class ShipmentFailedEvent {
  private UUID messageId;
  private UUID sagaId;
  private String orderId;
  private UUID correlationId;
  private String error;
  private FailAt failAt;

  public ShipmentFailedEvent() {
  }

  public ShipmentFailedEvent(UUID messageId, UUID sagaId, String orderId, UUID correlationId, String error, FailAt failAt) {
    this.messageId = messageId;
    this.sagaId = sagaId;
    this.orderId = orderId;
    this.correlationId = correlationId;
    this.error = error;
    this.failAt = failAt != null ? failAt : FailAt.NONE;
  }

  public UUID getMessageId() {
    return messageId;
  }

  public void setMessageId(UUID messageId) {
    this.messageId = messageId;
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
