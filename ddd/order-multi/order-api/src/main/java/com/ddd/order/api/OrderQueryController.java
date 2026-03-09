package com.ddd.order.api;

import com.ddd.order.query.OrderDTO;
import com.ddd.order.query.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Order query operations (read side — CQRS).
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderQueryController {

    private final OrderQueryService orderQueryService;

    @GetMapping
    public List<OrderDTO> getAllOrders() {
        return orderQueryService.findAll();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable String orderId) {
        return orderQueryService.findById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
