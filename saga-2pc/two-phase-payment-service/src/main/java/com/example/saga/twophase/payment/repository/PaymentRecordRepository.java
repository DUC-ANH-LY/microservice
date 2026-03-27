package com.example.saga.twophase.payment.repository;

import com.example.saga.twophase.payment.entity.PaymentRecord;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, UUID> {

  Optional<PaymentRecord> findByTransactionId(UUID transactionId);
}
