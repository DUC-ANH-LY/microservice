package com.example.saga.twophase.coordinator.core;

import com.example.saga.twophase.coordinator.client.ParticipantClient;
import com.example.saga.twophase.coordinator.entity.TransactionLog;
import com.example.saga.twophase.coordinator.repository.TransactionLogRepository;
import com.example.saga.twophase.shared.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TwoPhaseCoordinator {

  private final TransactionLogRepository transactionLogRepository;
  private final ParticipantClient participantClient;

  public TwoPhaseCoordinator(TransactionLogRepository transactionLogRepository,
                             ParticipantClient participantClient) {
    this.transactionLogRepository = transactionLogRepository;
    this.participantClient = participantClient;
  }

  @Transactional
  public TransactionResult execute(TransactionRequest request) {
    UUID transactionId = UUID.randomUUID();
    PrepareRequest prepareReq = new PrepareRequest(
        transactionId,
        request.orderId(),
        request.amount(),
        request.quantity(),
        request.address(),
        request.failAt()
    );
    CommitRequest commitReq = new CommitRequest(transactionId);
    RollbackRequest rollbackReq = new RollbackRequest(transactionId);

    TransactionLog log = new TransactionLog(transactionId, request.orderId(), TransactionLog.TransactionStatus.PREPARING);
    transactionLogRepository.save(log);

    String paymentUrl = participantClient.getUrls().getPayment();
    String inventoryUrl = participantClient.getUrls().getInventory();
    String shippingUrl = participantClient.getUrls().getShipping();

    List<String> preparedParticipants = new ArrayList<>();

    // Phase 1: Prepare (sequential: payment -> inventory -> shipping)
    if (!participantClient.prepare(paymentUrl, prepareReq)) {
      log.setStatus(TransactionLog.TransactionStatus.ABORTED);
      return TransactionResult.aborted(transactionId, "Payment prepare failed");
    }
    preparedParticipants.add("payment");

    if (!participantClient.prepare(inventoryUrl, prepareReq)) {
      rollbackAll(preparedParticipants, rollbackReq);
      log.setStatus(TransactionLog.TransactionStatus.ABORTED);
      return TransactionResult.aborted(transactionId, "Inventory prepare failed");
    }
    preparedParticipants.add("inventory");

    if (!participantClient.prepare(shippingUrl, prepareReq)) {
      rollbackAll(preparedParticipants, rollbackReq);
      log.setStatus(TransactionLog.TransactionStatus.ABORTED);
      return TransactionResult.aborted(transactionId, "Shipping prepare failed");
    }
    preparedParticipants.add("shipping");

    // Phase 2: Commit (all prepared)
    participantClient.commit(paymentUrl, commitReq);
    participantClient.commit(inventoryUrl, commitReq);
    participantClient.commit(shippingUrl, commitReq);

    log.setStatus(TransactionLog.TransactionStatus.COMMITTED);
    return TransactionResult.committed(transactionId);
  }

  private void rollbackAll(List<String> preparedParticipants, RollbackRequest rollbackReq) {
    if (preparedParticipants.contains("payment")) {
      participantClient.rollback(participantClient.getUrls().getPayment(), rollbackReq);
    }
    if (preparedParticipants.contains("inventory")) {
      participantClient.rollback(participantClient.getUrls().getInventory(), rollbackReq);
    }
    if (preparedParticipants.contains("shipping")) {
      participantClient.rollback(participantClient.getUrls().getShipping(), rollbackReq);
    }
  }

  public record TransactionResult(UUID transactionId, boolean committed, String message) {
    public static TransactionResult committed(UUID transactionId) {
      return new TransactionResult(transactionId, true, null);
    }

    public static TransactionResult aborted(UUID transactionId, String message) {
      return new TransactionResult(transactionId, false, message);
    }
  }
}
