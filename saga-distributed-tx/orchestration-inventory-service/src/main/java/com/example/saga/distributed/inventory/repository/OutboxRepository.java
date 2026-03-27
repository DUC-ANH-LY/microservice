package com.example.saga.distributed.inventory.repository;

import com.example.saga.distributed.inventory.entity.OutboxMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {

  @Query("SELECT o FROM OutboxMessage o WHERE o.sentAt IS NULL ORDER BY o.createdAt")
  List<OutboxMessage> findUnsentOrderByCreatedAt(org.springframework.data.domain.Pageable pageable);
}
