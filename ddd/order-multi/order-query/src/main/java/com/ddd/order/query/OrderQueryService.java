package com.ddd.order.query;

import com.ddd.order.infrastructure.persistence.OrderItemJpaEntity;
import com.ddd.order.infrastructure.persistence.OrderJpaEntity;
import com.ddd.order.infrastructure.persistence.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CQRS Read Side: Order Query Service.
 *
 * WRITE path: API → Application Service → Domain Model → Repository → DB
 * READ path:  API → Query Service → JPA Repository → DB  (no domain involved)
 */
@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderJpaRepository orderJpaRepository;

    @Transactional(readOnly = true)
    public Optional<OrderDTO> findById(String orderId) {
        return orderJpaRepository.findById(orderId).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> findAll() {
        return orderJpaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private OrderDTO toDTO(OrderJpaEntity entity) {
        List<OrderDTO.OrderItemDTO> itemDTOs = entity.getItems().stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .orderId(entity.getOrderId())
                .customerId(entity.getCustomerId())
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .currency(entity.getCurrency())
                .street(entity.getStreet())
                .city(entity.getCity())
                .country(entity.getCountry())
                .createdAt(entity.getCreatedAt())
                .items(itemDTOs)
                .build();
    }

    private OrderDTO.OrderItemDTO toItemDTO(OrderItemJpaEntity item) {
        BigDecimal subtotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        return OrderDTO.OrderItemDTO.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .currency(item.getCurrency())
                .subtotal(subtotal)
                .build();
    }
}
