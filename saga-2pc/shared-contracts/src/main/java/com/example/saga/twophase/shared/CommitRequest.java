package com.example.saga.twophase.shared;

import java.util.UUID;

/**
 * Request for a participant to commit (Phase 2).
 */
public record CommitRequest(UUID transactionId) {
  public CommitRequest {
    if (transactionId == null) {
      throw new IllegalArgumentException("transactionId required");
    }
  }
}
