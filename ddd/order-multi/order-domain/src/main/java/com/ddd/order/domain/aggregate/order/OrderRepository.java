package com.ddd.order.domain.aggregate.order;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for the Order aggregate.
 * Defined here in the domain layer — co-located with its aggregate root.
 * The infrastructure layer provides the concrete implementation.
 */
public interface OrderRepository {

    void save(Order order);

    Optional<Order> findById(OrderId orderId);

    List<Order> findAll();
}
