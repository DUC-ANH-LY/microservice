package com.example.saga.twophase.inventory.core;

import com.example.saga.twophase.inventory.entity.InventoryReservation;
import com.example.saga.twophase.inventory.entity.PreparedReservation;
import com.example.saga.twophase.inventory.repository.InventoryReservationRepository;
import com.example.saga.twophase.inventory.repository.PreparedReservationRepository;
import com.example.saga.twophase.shared.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class InventoryParticipantHandler {

  private final PreparedReservationRepository preparedRepository;
  private final InventoryReservationRepository reservationRepository;

  public InventoryParticipantHandler(PreparedReservationRepository preparedRepository,
                                    InventoryReservationRepository reservationRepository) {
    this.preparedRepository = preparedRepository;
    this.reservationRepository = reservationRepository;
  }

  @Transactional
  public void prepare(PrepareRequest request) {
    UUID txId = request.transactionId();

    if (preparedRepository.findByTransactionId(txId).isPresent()) {
      return;
    }

    if (reservationRepository.findByTransactionId(txId).isPresent()) {
      return;
    }

    if (request.failAt() == FailAt.INVENTORY) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Inventory prepare aborted (failAt=INVENTORY)");
    }

    PreparedReservation prepared = new PreparedReservation(
        txId,
        request.orderId(),
        request.quantity()
    );
    preparedRepository.save(prepared);
  }

  @Transactional
  public void commit(CommitRequest request) {
    UUID txId = request.transactionId();

    if (reservationRepository.findByTransactionId(txId).isPresent()) {
      return;
    }

    PreparedReservation prepared = preparedRepository.findByTransactionId(txId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Transaction not prepared"));

    InventoryReservation reservation = new InventoryReservation(
        prepared.getTransactionId(),
        prepared.getOrderId(),
        prepared.getQuantity()
    );
    reservationRepository.save(reservation);
    preparedRepository.delete(prepared);
  }

  @Transactional
  public void rollback(RollbackRequest request) {
    UUID txId = request.transactionId();
    preparedRepository.findByTransactionId(txId).ifPresent(preparedRepository::delete);
  }
}
