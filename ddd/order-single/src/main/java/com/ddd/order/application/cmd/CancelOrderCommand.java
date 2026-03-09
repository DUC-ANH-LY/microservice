package com.ddd.order.application.cmd;

/**
 * Command to cancel an existing Order.
 */
public record CancelOrderCommand(String orderId) {
}
