package com.example.saga.twophase.payment.core;

import com.example.saga.twophase.payment.entity.PaymentRecord;
import com.example.saga.twophase.payment.entity.PreparedTransaction;
import com.example.saga.twophase.payment.repository.PaymentRecordRepository;
import com.example.saga.twophase.payment.repository.PreparedTransactionRepository;
import com.example.saga.twophase.shared.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class PaymentParticipantHandler {

  private final PreparedTransactionRepository preparedTxRepository;
  private final PaymentRecordRepository paymentRecordRepository;

  public PaymentParticipantHandler(PreparedTransactionRepository preparedTxRepository,
                                  PaymentRecordRepository paymentRecordRepository) {
    this.preparedTxRepository = preparedTxRepository;
    this.paymentRecordRepository = paymentRecordRepository;
  }

  @Transactional
  public void prepare(PrepareRequest request) {
    UUID txId = request.transactionId();

    // Idempotent: already prepared
    if (preparedTxRepository.findByTransactionId(txId).isPresent()) {
      return;
    }

    // Already committed (retry after commit)
    if (paymentRecordRepository.findByTransactionId(txId).isPresent()) {
      return;
    }

    // FailAt simulation
    if (request.failAt() == FailAt.PAYMENT) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment prepare aborted (failAt=PAYMENT)");
    }

    PreparedTransaction prepared = new PreparedTransaction(
        txId,
        request.orderId(),
        request.amount()
    );
    preparedTxRepository.save(prepared);
  }

  @Transactional
  public void commit(CommitRequest request) {
    UUID txId = request.transactionId();

    // Idempotent: already committed
    if (paymentRecordRepository.findByTransactionId(txId).isPresent()) {
      return;
    }

    PreparedTransaction prepared = preparedTxRepository.findByTransactionId(txId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Transaction not prepared"));

    PaymentRecord record = new PaymentRecord(
        prepared.getTransactionId(),
        prepared.getOrderId(),
        prepared.getAmount()
    );
    paymentRecordRepository.save(record);
    preparedTxRepository.delete(prepared);
  }

  @Transactional
  public void rollback(RollbackRequest request) {
    UUID txId = request.transactionId();

    preparedTxRepository.findByTransactionId(txId).ifPresent(preparedTxRepository::delete);
  }
}
