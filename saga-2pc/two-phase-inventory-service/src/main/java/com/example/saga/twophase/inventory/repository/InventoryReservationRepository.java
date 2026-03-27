package com.example.saga.twophase.inventory.repository;

import com.example.saga.twophase.inventory.entity.InventoryReservation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {

  Optional<InventoryReservation> findByTransactionId(UUID transactionId);
}
