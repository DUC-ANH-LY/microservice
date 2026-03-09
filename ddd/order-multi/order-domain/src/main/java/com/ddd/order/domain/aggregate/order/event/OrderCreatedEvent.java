package com.ddd.order.domain.aggregate.order.event;

import com.ddd.order.domain.aggregate.order.Money;
import com.ddd.order.shared.DomainEvent;

import java.time.LocalDateTime;

/**
 * Domain Event raised when a new Order is successfully created.
 */
public record OrderCreatedEvent(
        String orderId,
        String customerId,
        Money totalAmount,
        LocalDateTime occurredOn
) implements DomainEvent {
}
