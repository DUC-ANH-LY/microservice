package com.example.saga.distributed.contracts.topic;

/**
 * Kafka topics for saga-distributed-tx (prefix: saga.distributed).
 */
public final class DistributedTopicNames {
  private DistributedTopicNames() {
  }

  public static final String PAYMENT_PROCESS_COMMAND = "saga.distributed.payment.commands.processPayment";
  public static final String PAYMENT_REFUND_COMMAND = "saga.distributed.payment.commands.refund";
  public static final String PAYMENT_SUCCEEDED_EVENT = "saga.distributed.payment.events.succeeded";
  public static final String PAYMENT_FAILED_EVENT = "saga.distributed.payment.events.failed";
  public static final String PAYMENT_REFUND_COMPLETED_EVENT = "saga.distributed.payment.events.refundCompleted";

  public static final String INVENTORY_RESERVE_COMMAND = "saga.distributed.inventory.commands.reserve";
  public static final String INVENTORY_RELEASE_COMMAND = "saga.distributed.inventory.commands.release";
  public static final String INVENTORY_RESERVED_EVENT = "saga.distributed.inventory.events.reserved";
  public static final String INVENTORY_FAILED_EVENT = "saga.distributed.inventory.events.failed";
  public static final String INVENTORY_RELEASED_EVENT = "saga.distributed.inventory.events.released";

  public static final String SHIPPING_SHIP_COMMAND = "saga.distributed.shipping.commands.ship";
  public static final String SHIPMENT_SHIPPED_EVENT = "saga.distributed.shipping.events.shipped";
  public static final String SHIPMENT_FAILED_EVENT = "saga.distributed.shipping.events.failed";
}
