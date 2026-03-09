package com.ddd.order.application.service;

import com.ddd.order.application.cmd.CancelOrderCommand;
import com.ddd.order.application.cmd.ConfirmOrderCommand;
import com.ddd.order.application.cmd.CreateOrderCommand;

/**
 * Application Service interface for Order command operations.
 *
 * Application Services orchestrate domain objects to fulfil a use case.
 * They are the entry point from the outside world into the domain model.
 * They contain no business logic — that lives in the domain.
 */
public interface OrderCmdService {

    /**
     * Creates a new order. Returns the generated orderId.
     */
    String createOrder(CreateOrderCommand command);

    /**
     * Confirms a pending order.
     */
    void confirmOrder(ConfirmOrderCommand command);

    /**
     * Cancels a pending or confirmed order.
     */
    void cancelOrder(CancelOrderCommand command);
}
