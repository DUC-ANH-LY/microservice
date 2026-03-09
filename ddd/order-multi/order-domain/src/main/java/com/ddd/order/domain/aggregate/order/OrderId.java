package com.ddd.order.domain.aggregate.order;

import com.ddd.order.shared.annotation.ValueObject;

import java.util.UUID;

/**
 * Value Object representing the unique identity of an Order.
 */
@ValueObject
public record OrderId(String value) {

    public OrderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OrderId value cannot be blank");
        }
    }

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID().toString());
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
