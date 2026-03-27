package com.example.saga.distributed.contracts;

public class SagaStartRequest {
  private String orderId;
  private FailAt failAt;

  public SagaStartRequest() {
  }

  public SagaStartRequest(String orderId, FailAt failAt) {
    this.orderId = orderId;
    this.failAt = failAt != null ? failAt : FailAt.NONE;
  }

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public FailAt getFailAt() {
    return failAt;
  }

  public void setFailAt(FailAt failAt) {
    this.failAt = failAt != null ? failAt : FailAt.NONE;
  }
}
