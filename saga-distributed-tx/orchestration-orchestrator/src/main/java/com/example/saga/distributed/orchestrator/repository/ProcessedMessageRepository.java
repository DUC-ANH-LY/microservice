package com.example.saga.distributed.orchestrator.repository;

import com.example.saga.distributed.orchestrator.entity.ProcessedMessage;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, UUID> {

  Optional<ProcessedMessage> findBySagaIdAndMessageId(UUID sagaId, UUID messageId);
}
