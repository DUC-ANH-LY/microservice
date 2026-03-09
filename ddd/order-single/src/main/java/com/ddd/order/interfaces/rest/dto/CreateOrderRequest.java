package com.ddd.order.interfaces.rest.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * HTTP request DTO for creating an order.
 * Converted to CreateOrderCommand before reaching the application layer.
 */
@Data
public class CreateOrderRequest {

    private String customerId;
    private List<OrderItemRequest> items;
    private String street;
    private String city;
    private String country;

    @Data
    public static class OrderItemRequest {
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private String currency = "USD";
    }
}
