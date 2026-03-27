package com.example.saga.twophase.inventory.repository;

import com.example.saga.twophase.inventory.entity.PreparedReservation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreparedReservationRepository extends JpaRepository<PreparedReservation, UUID> {

  Optional<PreparedReservation> findByTransactionId(UUID transactionId);
}
