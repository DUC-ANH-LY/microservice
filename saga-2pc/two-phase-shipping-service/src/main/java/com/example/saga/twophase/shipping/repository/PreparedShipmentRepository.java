package com.example.saga.twophase.shipping.repository;

import com.example.saga.twophase.shipping.entity.PreparedShipment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreparedShipmentRepository extends JpaRepository<PreparedShipment, UUID> {

  Optional<PreparedShipment> findByTransactionId(UUID transactionId);
}
