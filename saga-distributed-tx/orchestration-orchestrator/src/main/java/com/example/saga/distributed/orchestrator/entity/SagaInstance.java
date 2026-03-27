package com.example.saga.distributed.orchestrator.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "saga_instance")
public class SagaInstance {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "saga_id", nullable = false, unique = true)
  private UUID sagaId;

  @Column(name = "order_id", nullable = false)
  private String orderId;

  @Column(name = "correlation_id", nullable = false)
  private UUID correlationId;

  @Column(name = "fail_at", length = 32)
  private String failAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SagaStatus status;

  @Column(name = "steps_json", length = 2048)
  private String stepsJson;

  @Column(name = "compensations_pending")
  private int compensationsPending;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public enum SagaStatus {
    IN_PROGRESS,
    COMPLETED,
    FAILED
  }

  protected SagaInstance() {
  }

  public SagaInstance(UUID sagaId, String orderId, UUID correlationId, String failAt) {
    this.sagaId = sagaId;
    this.orderId = orderId;
    this.correlationId = correlationId;
    this.failAt = failAt != null ? failAt : "NONE";
    this.status = SagaStatus.IN_PROGRESS;
    this.stepsJson = "{\"PAYMENT\":\"PENDING\",\"INVENTORY\":\"PENDING\",\"SHIPMENT\":\"PENDING\"}";
    this.compensationsPending = 0;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public void setStep(String step, String value) {
    Map<String, String> steps = getStepsMap();
    steps.put(step, value);
    this.stepsJson = toJson(steps);
    this.updatedAt = Instant.now();
  }

  public void setStatus(SagaStatus status) {
    this.status = status;
    this.updatedAt = Instant.now();
  }

  public void setCompensationsPending(int n) {
    this.compensationsPending = n;
    this.updatedAt = Instant.now();
  }

  public int decrementCompensationsPending() {
    this.compensationsPending = Math.max(0, this.compensationsPending - 1);
    this.updatedAt = Instant.now();
    return this.compensationsPending;
  }

  public Map<String, String> getSteps() {
    return getStepsMap();
  }

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private Map<String, String> getStepsMap() {
    if (stepsJson == null || stepsJson.isEmpty()) {
      return new HashMap<>();
    }
    try {
      return OBJECT_MAPPER.readValue(stepsJson, new TypeReference<>() {});
    } catch (Exception e) {
      return new HashMap<>();
    }
  }

  private String toJson(Map<String, String> m) {
    try {
      return OBJECT_MAPPER.writeValueAsString(m);
    } catch (Exception e) {
      return "{}";
    }
  }

  public UUID getId() {
    return id;
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

  public String getFailAt() {
    return failAt;
  }

  public SagaStatus getStatus() {
    return status;
  }

  public int getCompensationsPending() {
    return compensationsPending;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
