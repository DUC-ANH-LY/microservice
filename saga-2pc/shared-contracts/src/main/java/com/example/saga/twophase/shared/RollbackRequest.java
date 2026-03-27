package com.example.saga.twophase.shared;

import java.util.UUID;

/**
 * Request for a participant to rollback (Phase 2).
 */
public record RollbackRequest(UUID transactionId) {
  public RollbackRequest {
    if (transactionId == null) {
      throw new IllegalArgumentException("transactionId required");
    }
  }
}
