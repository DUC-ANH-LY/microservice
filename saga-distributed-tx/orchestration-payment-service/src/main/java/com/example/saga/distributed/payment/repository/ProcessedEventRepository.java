package com.example.saga.distributed.payment.repository;

import com.example.saga.distributed.payment.entity.ProcessedEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

  boolean existsByMessageId(UUID messageId);
}
