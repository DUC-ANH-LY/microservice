package com.example.saga.orchestration.orchestrator.core;

import com.example.saga.shared.contracts.FailAt;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SagaInstance {
  private final UUID sagaId;
  private final String orderId;
  private final UUID correlationId;
  private final FailAt failAt;

  private volatile SagaStatus status = SagaStatus.IN_PROGRESS;

  // step name -> status
  private final ConcurrentHashMap<String, String> steps = new ConcurrentHashMap<>();

  // How many compensation steps still need to complete before we can mark FAILED.
  private volatile int compensationsPending = 0;

  public SagaInstance(UUID sagaId, String orderId, UUID correlationId, FailAt failAt) {
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

  public SagaStatus getStatus() {
    return status;
  }

  public void setStatus(SagaStatus status) {
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

