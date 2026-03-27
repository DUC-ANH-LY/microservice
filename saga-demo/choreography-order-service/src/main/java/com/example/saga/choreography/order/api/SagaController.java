package com.example.saga.choreography.order.api;

import com.example.saga.choreography.order.core.ChoreographySagaTracker;
import com.example.saga.shared.contracts.SagaStartRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/sagas")
public class SagaController {
  private final ChoreographySagaTracker tracker;

  public SagaController(ChoreographySagaTracker tracker) {
    this.tracker = tracker;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public StartSagaResponse startSaga(@RequestBody SagaStartRequest request) {
    UUID sagaId = tracker.startSaga(request);
    return new StartSagaResponse(sagaId);
  }

  @GetMapping("/{sagaId}")
  public SagaStatusResponse getStatus(@PathVariable("sagaId") UUID sagaId) {
    return tracker.getStatus(sagaId);
  }

  public record StartSagaResponse(UUID sagaId) {
  }

  public record SagaStatusResponse(UUID sagaId, String status, java.util.List<StepStatus> steps) {
  }

  public record StepStatus(String step, String status) {
  }
}

