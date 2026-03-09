package com.ddd.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for OrderJpaEntity.
 * Used by OrderRepositoryImpl (write side) and OrderQueryService (read side).
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, String> {
}
