package com.ddd.order.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * JPA entity for persisting OrderItem data.
 * This is an infrastructure concern — completely separate from the domain's OrderItem entity.
 * The domain model is mapped to/from this entity by OrderRepositoryImpl.
 */
@jakarta.persistence.Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, length = 3)
    private String currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;
}
