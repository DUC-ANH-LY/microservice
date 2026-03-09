package com.ddd.order.application.cmd;

/**
 * Command to confirm an existing Order.
 */
public record ConfirmOrderCommand(String orderId) {
}
