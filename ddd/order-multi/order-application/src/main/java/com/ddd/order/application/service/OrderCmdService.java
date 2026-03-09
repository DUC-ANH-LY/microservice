package com.ddd.order.application.service;

import com.ddd.order.application.cmd.CancelOrderCommand;
import com.ddd.order.application.cmd.ConfirmOrderCommand;
import com.ddd.order.application.cmd.CreateOrderCommand;

/**
 * Application Service interface for Order command operations.
 * Defines use cases; the implementation orchestrates domain objects.
 */
public interface OrderCmdService {

    String createOrder(CreateOrderCommand command);

    void confirmOrder(ConfirmOrderCommand command);

    void cancelOrder(CancelOrderCommand command);
}
