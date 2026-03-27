package com.example.saga.distributed.orchestrator.repository;

import com.example.saga.distributed.orchestrator.entity.SagaInstance;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaInstanceRepository extends JpaRepository<SagaInstance, UUID> {

  Optional<SagaInstance> findBySagaId(UUID sagaId);
}
