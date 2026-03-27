package com.example.saga.shared.contracts.topic;

/**
 * Kafka topics used by the Saga choreography demo.
 */
public final class ChoreographyTopicNames {
  private ChoreographyTopicNames() {
  }

  // Order
  public static final String ORDER_CREATED_EVENT = "saga.choreography.order.events.orderCreated";

  // Payment
  public static final String PAYMENT_SUCCEEDED_EVENT = "saga.choreography.payment.events.succeeded";
  public static final String PAYMENT_FAILED_EVENT = "saga.choreography.payment.events.failed";
  public static final String REFUND_COMMAND = "saga.choreography.payment.commands.refund";
  public static final String REFUND_COMPLETED_EVENT = "saga.choreography.payment.events.refundCompleted";

  // Inventory
  public static final String INVENTORY_RESERVED_EVENT = "saga.choreography.inventory.events.reserved";
  public static final String INVENTORY_FAILED_EVENT = "saga.choreography.inventory.events.failed";
  public static final String INVENTORY_RELEASE_COMMAND = "saga.choreography.inventory.commands.release";
  public static final String INVENTORY_RELEASED_EVENT = "saga.choreography.inventory.events.released";

  // Shipping
  public static final String SHIPMENT_SHIPPED_EVENT = "saga.choreography.shipping.events.shipped";
  public static final String SHIPMENT_FAILED_EVENT = "saga.choreography.shipping.events.failed";
}

