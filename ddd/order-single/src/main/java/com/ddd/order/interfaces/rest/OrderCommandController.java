package com.ddd.order.interfaces.rest;

import com.ddd.order.application.cmd.CancelOrderCommand;
import com.ddd.order.application.cmd.ConfirmOrderCommand;
import com.ddd.order.application.cmd.CreateOrderCommand;
import com.ddd.order.application.service.OrderCmdService;
import com.ddd.order.interfaces.rest.dto.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for Order command operations (write side).
 * Translates HTTP requests to application commands.
 * Contains NO business logic.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderCommandController {

    private final OrderCmdService orderCmdService;

    /**
     * POST /api/orders
     * Create a new order. Returns the generated orderId.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = CreateOrderCommand.builder()
                .customerId(request.getCustomerId())
                .items(request.getItems().stream()
                        .map(i -> CreateOrderCommand.OrderItemCmd.builder()
                                .productId(i.getProductId())
                                .productName(i.getProductName())
                                .quantity(i.getQuantity())
                                .unitPrice(i.getUnitPrice())
                                .currency(i.getCurrency())
                                .build())
                        .collect(Collectors.toList()))
                .street(request.getStreet())
                .city(request.getCity())
                .country(request.getCountry())
                .build();

        String orderId = orderCmdService.createOrder(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("orderId", orderId));
    }

    /**
     * POST /api/orders/{orderId}/confirm
     * Confirm a pending order.
     */
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Void> confirmOrder(@PathVariable String orderId) {
        orderCmdService.confirmOrder(new ConfirmOrderCommand(orderId));
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/orders/{orderId}/cancel
     * Cancel an order.
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId) {
        orderCmdService.cancelOrder(new CancelOrderCommand(orderId));
        return ResponseEntity.ok().build();
    }
}
