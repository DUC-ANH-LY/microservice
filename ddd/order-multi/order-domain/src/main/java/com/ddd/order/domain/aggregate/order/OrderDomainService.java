package com.ddd.order.domain.aggregate.order;

import java.util.List;

/**
 * Domain Service for the Order aggregate.
 * Handles business logic that does not naturally fit within a single aggregate.
 *
 * Plain Java class — no Spring annotations. Registered as a bean via
 * an @Configuration class in the infrastructure layer to keep domain framework-free.
 */
public class OrderDomainService {

    public Order createOrder(String customerId, List<OrderItem> items, Address shippingAddress) {
        validateCustomerId(customerId);
        validateItems(items);
        validateAddress(shippingAddress);
        validateCurrencyConsistency(items);

        return Order.create(OrderId.generate(), customerId, items, shippingAddress);
    }

    private void validateCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required to create an order.");
        }
    }

    private void validateItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one item.");
        }
        for (OrderItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "Item quantity must be positive for product: " + item.getProductId());
            }
        }
    }

    private void validateAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Shipping address is required.");
        }
    }

    private void validateCurrencyConsistency(List<OrderItem> items) {
        long distinctCurrencies = items.stream()
                .map(i -> i.getUnitPrice().currency())
                .distinct()
                .count();
        if (distinctCurrencies > 1) {
            throw new IllegalArgumentException("All order items must use the same currency.");
        }
    }
}
