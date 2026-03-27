package com.example.saga.twophase.shipping.core;

import com.example.saga.twophase.shared.*;
import com.example.saga.twophase.shipping.entity.PreparedShipment;
import com.example.saga.twophase.shipping.entity.ShippingRecord;
import com.example.saga.twophase.shipping.repository.PreparedShipmentRepository;
import com.example.saga.twophase.shipping.repository.ShippingRecordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class ShippingParticipantHandler {

  private final PreparedShipmentRepository preparedRepository;
  private final ShippingRecordRepository shippingRecordRepository;

  public ShippingParticipantHandler(PreparedShipmentRepository preparedRepository,
                                    ShippingRecordRepository shippingRecordRepository) {
    this.preparedRepository = preparedRepository;
    this.shippingRecordRepository = shippingRecordRepository;
  }

  @Transactional
  public void prepare(PrepareRequest request) {
    UUID txId = request.transactionId();

    if (preparedRepository.findByTransactionId(txId).isPresent()) {
      return;
    }

    if (shippingRecordRepository.findByTransactionId(txId).isPresent()) {
      return;
    }

    if (request.failAt() == FailAt.SHIPMENT) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Shipping prepare aborted (failAt=SHIPMENT)");
    }

    PreparedShipment prepared = new PreparedShipment(
        txId,
        request.orderId(),
        request.address()
    );
    preparedRepository.save(prepared);
  }

  @Transactional
  public void commit(CommitRequest request) {
    UUID txId = request.transactionId();

    if (shippingRecordRepository.findByTransactionId(txId).isPresent()) {
      return;
    }

    PreparedShipment prepared = preparedRepository.findByTransactionId(txId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Transaction not prepared"));

    ShippingRecord record = new ShippingRecord(
        prepared.getTransactionId(),
        prepared.getOrderId(),
        prepared.getAddress()
    );
    shippingRecordRepository.save(record);
    preparedRepository.delete(prepared);
  }

  @Transactional
  public void rollback(RollbackRequest request) {
    UUID txId = request.transactionId();
    preparedRepository.findByTransactionId(txId).ifPresent(preparedRepository::delete);
  }
}
