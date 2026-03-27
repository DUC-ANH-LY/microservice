package com.example.saga.distributed.shipping.repository;

import com.example.saga.distributed.shipping.entity.ProcessedEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

  boolean existsByMessageId(UUID messageId);
}
