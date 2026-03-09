package com.ddd.order.domain.aggregate.order;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for the Order aggregate.
 * Defined in the domain layer — the infrastructure layer provides the implementation.
 *
 * Following DDD: the repository interface lives with its aggregate, not in the infrastructure.
 * The implementation (adapter) is in the infrastructure layer.
 */
public interface OrderRepository {

    /**
     * Persists a new or updated Order aggregate.
     */
    void save(Order order);

    /**
     * Finds an Order by its identity.
     */
    Optional<Order> findById(OrderId orderId);

    /**
     * Returns all orders.
     */
    List<Order> findAll();
}
