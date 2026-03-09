package com.ddd.order.application.cmd;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Command to create a new Order.
 * Commands are plain data objects that carry the intent of the caller.
 * They are validated and executed by the Application Service.
 */
@Data
@Builder
public class CreateOrderCommand {

    private String customerId;
    private List<OrderItemCmd> items;
    private String street;
    private String city;
    private String country;

    @Data
    @Builder
    public static class OrderItemCmd {
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private String currency;
    }
}
