package com.ddd.order.domain.aggregate.order;

import com.ddd.order.common.annotation.ValueObject;

/**
 * Value Object representing a shipping address.
 * Immutable — if the address changes, a new Address instance is created.
 */
@ValueObject
public record Address(String street, String city, String country) {

    public Address {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street cannot be blank");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City cannot be blank");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country cannot be blank");
        }
    }

    @Override
    public String toString() {
        return street + ", " + city + ", " + country;
    }
}
