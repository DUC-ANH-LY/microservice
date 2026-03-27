package com.example.saga.twophase.payment.repository;

import com.example.saga.twophase.payment.entity.PreparedTransaction;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreparedTransactionRepository extends JpaRepository<PreparedTransaction, UUID> {

  Optional<PreparedTransaction> findByTransactionId(UUID transactionId);
}
