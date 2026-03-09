package com.ddd.order.application.service.impl;

import com.ddd.order.application.cmd.CancelOrderCommand;
import com.ddd.order.application.cmd.ConfirmOrderCommand;
import com.ddd.order.application.cmd.CreateOrderCommand;
import com.ddd.order.application.service.OrderCmdService;
import com.ddd.order.domain.aggregate.order.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Service implementation for Order command operations.
 *
 * Responsibilities:
 * 1. Translate incoming commands into domain calls
 * 2. Load/save aggregates via repository
 * 3. Publish domain events after the aggregate is saved
 *
 * No business logic here — that belongs in the domain.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCmdServiceImpl implements OrderCmdService {

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public String createOrder(CreateOrderCommand command) {
        List<OrderItem> items = command.getItems().stream()
                .map(i -> new OrderItem(
                        i.getProductId(),
                        i.getProductName(),
                        i.getQuantity(),
                        Money.of(i.getUnitPrice(), i.getCurrency())))
                .collect(Collectors.toList());

        Address shippingAddress = new Address(command.getStreet(), command.getCity(), command.getCountry());

        // Delegate creation + validation to the Domain Service
        Order order = orderDomainService.createOrder(command.getCustomerId(), items, shippingAddress);
        orderRepository.save(order);

        // Publish domain events after successful persistence
        order.pullDomainEvents().forEach(eventPublisher::publishEvent);

        log.info("Order created successfully: orderId={}, customerId={}",
                order.getOrderId(), order.getCustomerId());
        return order.getOrderId().value();
    }

    @Override
    @Transactional
    public void confirmOrder(ConfirmOrderCommand command) {
        Order order = orderRepository.findById(OrderId.of(command.orderId()))
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.orderId()));

        order.confirm();
        orderRepository.save(order);

        log.info("Order confirmed: orderId={}", command.orderId());
    }

    @Override
    @Transactional
    public void cancelOrder(CancelOrderCommand command) {
        Order order = orderRepository.findById(OrderId.of(command.orderId()))
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.orderId()));

        order.cancel();
        orderRepository.save(order);

        log.info("Order cancelled: orderId={}", command.orderId());
    }
}
