package com.ddd.order.domain.aggregate.order;

import com.ddd.order.domain.aggregate.order.event.OrderCreatedEvent;
import com.ddd.order.shared.annotation.AggregateRoot;
import lombok.Getter;

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

    private final List<Object> domainEvents = new ArrayList<>();

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

    public static Order create(OrderId orderId,
                               String customerId,
                               List<OrderItem> items,
                               Address shippingAddress) {
        Order order = new Order(orderId, customerId, items, shippingAddress, LocalDateTime.now());
        order.domainEvents.add(new OrderCreatedEvent(
                orderId.value(), customerId, order.totalAmount, order.createdAt));
        return order;
    }

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

    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING orders can be confirmed. Current status: " + this.status);
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled.");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

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
