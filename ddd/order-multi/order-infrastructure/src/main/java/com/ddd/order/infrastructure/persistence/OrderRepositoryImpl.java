package com.ddd.order.infrastructure.persistence;

import com.ddd.order.domain.aggregate.order.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Infrastructure adapter implementing the domain's OrderRepository interface.
 * Translates between domain objects and JPA entities (anti-corruption layer).
 */
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    @Override
    public void save(Order order) {
        OrderJpaEntity entity = toEntity(order);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return jpaRepository.findById(orderId.value()).map(this::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private OrderJpaEntity toEntity(Order order) {
        List<OrderItemJpaEntity> itemEntities = order.getItems().stream()
                .map(item -> OrderItemJpaEntity.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice().amount())
                        .currency(item.getUnitPrice().currency())
                        .build())
                .collect(Collectors.toList());

        OrderJpaEntity entity = OrderJpaEntity.builder()
                .orderId(order.getOrderId().value())
                .customerId(order.getCustomerId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount().amount())
                .currency(order.getTotalAmount().currency())
                .street(order.getShippingAddress().street())
                .city(order.getShippingAddress().city())
                .country(order.getShippingAddress().country())
                .createdAt(order.getCreatedAt())
                .items(itemEntities)
                .build();

        entity.getItems().forEach(i -> i.setOrder(entity));
        return entity;
    }

    private Order toDomain(OrderJpaEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(i -> new OrderItem(
                        i.getProductId(),
                        i.getProductName(),
                        i.getQuantity(),
                        Money.of(i.getUnitPrice(), i.getCurrency())))
                .collect(Collectors.toList());

        return Order.reconstitute(
                OrderId.of(entity.getOrderId()),
                entity.getCustomerId(),
                items,
                new Address(entity.getStreet(), entity.getCity(), entity.getCountry()),
                OrderStatus.valueOf(entity.getStatus()),
                Money.of(entity.getTotalAmount(), entity.getCurrency()),
                entity.getCreatedAt());
    }
}
