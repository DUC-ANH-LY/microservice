package com.ddd.order.domain.aggregate.customer;

import com.ddd.order.common.annotation.ValueObject;

import java.util.UUID;

/**
 * Value Object representing the unique identity of a Customer.
 */
@ValueObject
public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CustomerId cannot be blank");
        }
    }

    public static CustomerId generate() {
        return new CustomerId(UUID.randomUUID().toString());
    }

    public static CustomerId of(String value) {
        return new CustomerId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
