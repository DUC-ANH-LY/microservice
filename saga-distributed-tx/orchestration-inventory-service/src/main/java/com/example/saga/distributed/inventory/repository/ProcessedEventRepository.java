package com.example.saga.distributed.inventory.repository;

import com.example.saga.distributed.inventory.entity.ProcessedEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

  boolean existsByMessageId(UUID messageId);
}
