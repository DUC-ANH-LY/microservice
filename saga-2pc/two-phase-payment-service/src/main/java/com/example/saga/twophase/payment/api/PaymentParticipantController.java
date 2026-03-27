package com.example.saga.twophase.payment.api;

import com.example.saga.twophase.payment.core.PaymentParticipantHandler;
import com.example.saga.twophase.shared.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentParticipantController {

  private final PaymentParticipantHandler handler;

  public PaymentParticipantController(PaymentParticipantHandler handler) {
    this.handler = handler;
  }

  @PostMapping("/prepare")
  public ResponseEntity<Void> prepare(@RequestBody PrepareRequest request) {
    handler.prepare(request);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/commit")
  public ResponseEntity<Void> commit(@RequestBody CommitRequest request) {
    handler.commit(request);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/rollback")
  public ResponseEntity<Void> rollback(@RequestBody RollbackRequest request) {
    handler.rollback(request);
    return ResponseEntity.ok().build();
  }
}
