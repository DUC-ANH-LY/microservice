package com.example.saga.orchestration.orchestrator.api;

import com.example.saga.orchestration.orchestrator.core.SagaOrchestrator;
import com.example.saga.shared.contracts.SagaStartRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/sagas")
public class SagaController {
  private final SagaOrchestrator sagaOrchestrator;

  public SagaController(SagaOrchestrator sagaOrchestrator) {
    this.sagaOrchestrator = sagaOrchestrator;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public StartSagaResponse startSaga(@RequestBody SagaStartRequest request) {
    UUID sagaId = sagaOrchestrator.startSaga(request);
    return new StartSagaResponse(sagaId);
  }

  @GetMapping("/{sagaId}")
  public SagaStatusResponse getStatus(@PathVariable("sagaId") UUID sagaId) {
    return sagaOrchestrator.getStatus(sagaId);
  }

  public record StartSagaResponse(UUID sagaId) {
  }

  public record SagaStatusResponse(UUID sagaId, String status, java.util.List<StepStatus> steps) {
  }

  public record StepStatus(String step, String status) {
  }
}

