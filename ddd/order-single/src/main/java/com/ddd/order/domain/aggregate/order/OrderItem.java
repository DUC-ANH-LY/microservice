package com.ddd.order.domain.aggregate.order;

import com.ddd.order.common.annotation.Entity;
import lombok.Getter;

/**
 * Entity representing a line item within an Order aggregate.
 * OrderItem is part of the Order aggregate and is only accessible through the Order root.
 */
@Entity
@Getter
public class OrderItem {

    private final String productId;
    private final String productName;
    private final int quantity;
    private final Money unitPrice;

    public OrderItem(String productId, String productName, int quantity, Money unitPrice) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be blank");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, was: " + quantity);
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("Unit price cannot be null");
        }
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    /**
     * Computes the subtotal for this line item.
     */
    public Money subtotal() {
        return unitPrice.multiply(quantity);
    }
}
