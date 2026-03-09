package com.ddd.order.domain.aggregate.order;

import com.ddd.order.common.annotation.AggregateRoot;
import com.ddd.order.domain.aggregate.order.event.OrderCreatedEvent;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root for the Order aggregate.
 *
 * Responsibilities:
 * - Holds all order line items (OrderItem entities)
 * - Enforces business rules (state transitions, invariants)
 * - Raises Domain Events on state changes
 * - Is the only entry point into the aggregate
 *
 * External objects must not reference OrderItem directly; always go through Order.
 */
@AggregateRoot
@Getter
public class Order {

    private final OrderId orderId;
    private final String customerId;
    private final List<OrderItem> items;
    private final Address shippingAddress;
    private OrderStatus status;
    private Money totalAmount;
    private final LocalDateTime createdAt;

    /** Accumulated domain events — cleared after publishing. */
    private final List<Object> domainEvents = new ArrayList<>();

    // ── Private constructor: use factory methods ─────────────────────────────

    private Order(OrderId orderId,
                  String customerId,
                  List<OrderItem> items,
                  Address shippingAddress,
                  LocalDateTime createdAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.PENDING;
        this.createdAt = createdAt;
        this.totalAmount = calculateTotal();
    }

    // ── Factory: create new order ─────────────────────────────────────────────

    /**
     * Creates a brand-new Order and raises OrderCreatedEvent.
     */
    public static Order create(OrderId orderId,
                               String customerId,
                               List<OrderItem> items,
                               Address shippingAddress) {
        Order order = new Order(orderId, customerId, items, shippingAddress, LocalDateTime.now());
        order.domainEvents.add(new OrderCreatedEvent(
                orderId.value(), customerId, order.totalAmount, order.createdAt));
        return order;
    }

    // ── Factory: reconstitute from persistence ───────────────────────────────

    /**
     * Rebuilds an Order from its persisted state.
     * No events are raised — the order already exists.
     */
    public static Order reconstitute(OrderId orderId,
                                     String customerId,
                                     List<OrderItem> items,
                                     Address shippingAddress,
                                     OrderStatus status,
                                     Money totalAmount,
                                     LocalDateTime createdAt) {
        Order order = new Order(orderId, customerId, items, shippingAddress, createdAt);
        order.status = status;
        order.totalAmount = totalAmount;
        return order;
    }

    // ── Business behaviour ────────────────────────────────────────────────────

    /**
     * Confirms the order. Only valid from PENDING state.
     */
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING orders can be confirmed. Current status: " + this.status);
        }
        this.status = OrderStatus.CONFIRMED;
    }

    /**
     * Cancels the order. Valid from PENDING or CONFIRMED state.
     */
    public void cancel() {
        if (this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled.");
        }
        this.status = OrderStatus.CANCELLED;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Returns and clears accumulated domain events.
     * Call this after saving the aggregate so events can be published.
     */
    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    private Money calculateTotal() {
        return items.stream()
                .map(OrderItem::subtotal)
                .reduce(Money.zero("USD"), Money::add);
    }
}
