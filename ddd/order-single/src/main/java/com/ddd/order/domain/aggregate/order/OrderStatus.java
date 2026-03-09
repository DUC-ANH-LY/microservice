package com.ddd.order.domain.aggregate.order;

/**
 * Enumeration of possible Order lifecycle states.
 *
 * State transitions:
 *   PENDING → CONFIRMED
 *   PENDING → CANCELLED
 *   CONFIRMED → CANCELLED
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}
