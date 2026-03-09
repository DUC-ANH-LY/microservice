package com.ddd.order.shared;

import java.time.LocalDateTime;

/**
 * Marker interface for all Domain Events.
 * A Domain Event represents something meaningful that happened in the domain.
 * Events are named in past tense (e.g., OrderCreated, OrderConfirmed).
 */
public interface DomainEvent {

    /**
     * The moment this event occurred.
     */
    LocalDateTime occurredOn();
}
