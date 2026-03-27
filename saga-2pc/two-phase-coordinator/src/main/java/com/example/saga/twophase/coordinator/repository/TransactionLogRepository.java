package com.example.saga.twophase.coordinator.repository;

import com.example.saga.twophase.coordinator.entity.TransactionLog;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, UUID> {

  Optional<TransactionLog> findByTransactionId(UUID transactionId);
}
