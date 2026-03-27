package com.example.saga.choreography.order.core;

import com.example.saga.shared.contracts.FailAt;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChoreographySagaInstance {
  private final UUID sagaId;
  private final String orderId;
  private final UUID correlationId;
  private final FailAt failAt;

  private volatile ChoreographySagaStatus status = ChoreographySagaStatus.IN_PROGRESS;
  private final ConcurrentHashMap<String, String> steps = new ConcurrentHashMap<>();

  private volatile int compensationsPending = 0;

  public ChoreographySagaInstance(UUID sagaId, String orderId, UUID correlationId, FailAt failAt) {
    this.sagaId = sagaId;
    this.orderId = orderId;
    this.correlationId = correlationId;
    this.failAt = failAt;
  }

  public UUID getSagaId() {
    return sagaId;
  }

  public String getOrderId() {
    return orderId;
  }

  public UUID getCorrelationId() {
    return correlationId;
  }

  public FailAt getFailAt() {
    return failAt;
  }

  public ChoreographySagaStatus getStatus() {
    return status;
  }

  public void setStatus(ChoreographySagaStatus status) {
    this.status = status;
  }

  public Map<String, String> getSteps() {
    return steps;
  }

  public void setStep(String step, String stepStatus) {
    steps.put(step, stepStatus);
  }

  public int getCompensationsPending() {
    return compensationsPending;
  }

  public void setCompensationsPending(int compensationsPending) {
    this.compensationsPending = compensationsPending;
  }

  public int decrementCompensationsPending() {
    int next = Math.max(0, compensationsPending - 1);
    compensationsPending = next;
    return next;
  }
}

