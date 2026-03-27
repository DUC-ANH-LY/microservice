package com.example.saga.twophase.coordinator.api;

import com.example.saga.twophase.coordinator.core.TwoPhaseCoordinator;
import com.example.saga.twophase.coordinator.core.TwoPhaseCoordinator.TransactionResult;
import com.example.saga.twophase.coordinator.repository.TransactionLogRepository;
import com.example.saga.twophase.shared.TransactionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

  private final TwoPhaseCoordinator coordinator;
  private final TransactionLogRepository transactionLogRepository;

  public TransactionController(TwoPhaseCoordinator coordinator,
                               TransactionLogRepository transactionLogRepository) {
    this.coordinator = coordinator;
    this.transactionLogRepository = transactionLogRepository;
  }

  @PostMapping
  public ResponseEntity<?> createTransaction(@RequestBody TransactionRequest request) {
    try {
      TransactionResult result = coordinator.execute(request);
      if (result.committed()) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of(
                "transactionId", result.transactionId().toString(),
                "status", "COMMITTED"
            ));
      } else {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of(
                "transactionId", result.transactionId().toString(),
                "status", "ABORTED",
                "message", result.message() != null ? result.message() : "Transaction aborted"
            ));
      }
    } catch (Exception e) {
      return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/{transactionId}")
  public ResponseEntity<?> getStatus(@PathVariable UUID transactionId) {
    return transactionLogRepository.findByTransactionId(transactionId)
        .map(log -> ResponseEntity.ok(Map.of(
            "transactionId", log.getTransactionId().toString(),
            "orderId", log.getOrderId(),
            "status", log.getStatus().name()
        )))
        .orElse(ResponseEntity.notFound().build());
  }
}
