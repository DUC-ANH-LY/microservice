package com.ddd.order.domain.aggregate.order;

/**
 * Enumeration of possible Order lifecycle states.
 *
 * Transitions: PENDING → CONFIRMED, PENDING → CANCELLED, CONFIRMED → CANCELLED
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}
