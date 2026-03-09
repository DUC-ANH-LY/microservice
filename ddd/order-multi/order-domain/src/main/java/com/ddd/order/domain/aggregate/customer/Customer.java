package com.ddd.order.domain.aggregate.customer;

import com.ddd.order.shared.annotation.AggregateRoot;
import lombok.Getter;

/**
 * Aggregate Root for the Customer aggregate.
 * Referenced from the Order aggregate by CustomerId only (no direct object reference).
 */
@AggregateRoot
@Getter
public class Customer {

    private final CustomerId customerId;
    private final String name;
    private final String email;

    public Customer(CustomerId customerId, String name, String email) {
        if (customerId == null) throw new IllegalArgumentException("CustomerId is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Customer name is required");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Customer email is required");

        this.customerId = customerId;
        this.name = name;
        this.email = email;
    }
}
