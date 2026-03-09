package com.ddd.order.domain.aggregate.order.event;

import com.ddd.order.common.DomainEvent;
import com.ddd.order.domain.aggregate.order.Money;

import java.time.LocalDateTime;

/**
 * Domain Event raised when a new Order is successfully created.
 * Downstream consumers (e.g., notification service, audit log) can react to this event.
 */
public record OrderCreatedEvent(
        String orderId,
        String customerId,
        Money totalAmount,
        LocalDateTime occurredOn
) implements DomainEvent {
}
