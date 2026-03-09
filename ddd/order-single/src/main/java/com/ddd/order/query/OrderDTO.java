package com.ddd.order.query;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Read model (DTO) for Order queries — CQRS read side.
 *
 * This is a flat, denormalized projection optimised for display.
 * It bypasses the domain model entirely — built directly from JPA entity data.
 * Changes to this DTO do NOT affect the domain model.
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
