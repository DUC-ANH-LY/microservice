package com.example.saga.distributed.orchestrator.api;

import com.example.saga.distributed.contracts.SagaStartRequest;
import com.example.saga.distributed.orchestrator.core.SagaOrchestrator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
  public SagaStatusResponse getStatus(@PathVariable UUID sagaId) {
    return sagaOrchestrator.getStatus(sagaId);
  }

  public record StartSagaResponse(UUID sagaId) {
  }

  public record SagaStatusResponse(UUID sagaId, String status, List<StepStatus> steps) {
  }

  public record StepStatus(String step, String status) {
  }
}
