package com.example.saga.twophase.shipping.repository;

import com.example.saga.twophase.shipping.entity.ShippingRecord;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingRecordRepository extends JpaRepository<ShippingRecord, UUID> {

  Optional<ShippingRecord> findByTransactionId(UUID transactionId);
}
