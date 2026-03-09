package com.ddd.order.query;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Read model (DTO) for Order queries — CQRS read side.
 * Flat denormalized projection built directly from JPA entities.
 */
@Data
@Builder
public class OrderDTO {

    private String orderId;
    private String customerId;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private String street;
    private String city;
    private String country;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;

    @Data
    @Builder
    public static class OrderItemDTO {
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private String currency;
        private BigDecimal subtotal;
    }
}
