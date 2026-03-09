package com.ddd.order.interfaces.rest;

import com.ddd.order.query.OrderDTO;
import com.ddd.order.query.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Order query operations (read side — CQRS).
 * Reads go directly to the query service, bypassing the domain model.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderQueryController {

    private final OrderQueryService orderQueryService;

    /**
     * GET /api/orders
     * Returns all orders (flat DTOs, no domain model involved).
     */
    @GetMapping
    public List<OrderDTO> getAllOrders() {
        return orderQueryService.findAll();
    }

    /**
     * GET /api/orders/{orderId}
     * Returns a single order detail.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable String orderId) {
        return orderQueryService.findById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
