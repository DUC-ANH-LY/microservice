package com.ddd.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for OrderJpaEntity.
 * Used by both the write side (OrderRepositoryImpl) and the read side (OrderQueryService).
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, String> {
}
