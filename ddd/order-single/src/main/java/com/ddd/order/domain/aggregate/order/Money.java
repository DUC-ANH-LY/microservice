package com.ddd.order.domain.aggregate.order;

import com.ddd.order.common.annotation.ValueObject;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object representing a monetary amount with currency.
 * Immutable and self-validating.
 */
@ValueObject
public record Money(BigDecimal amount, String currency) {

    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency cannot be blank");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money ofUSD(BigDecimal amount) {
        return new Money(amount, "USD");
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot add money with different currencies: " + this.currency + " vs " + other.currency);
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }

    @Override
    public String toString() {
        return amount + " " + currency;
    }
}
