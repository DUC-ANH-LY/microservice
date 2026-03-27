package com.example.saga.distributed.contracts.orchestration;

import com.example.saga.distributed.contracts.FailAt;

import java.util.UUID;

public class ReserveInventoryCommand {
  private UUID messageId;
  private UUID sagaId;
  private String orderId;
  private UUID correlationId;
  private FailAt failAt;

  public ReserveInventoryCommand() {
  }

  public ReserveInventoryCommand(UUID messageId, UUID sagaId, String orderId, UUID correlationId, FailAt failAt) {
    this.messageId = messageId;
    this.sagaId = sagaId;
    this.orderId = orderId;
    this.correlationId = correlationId;
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

  public FailAt getFailAt() {
    return failAt;
  }

  public void setFailAt(FailAt failAt) {
    this.failAt = failAt;
  }
}
