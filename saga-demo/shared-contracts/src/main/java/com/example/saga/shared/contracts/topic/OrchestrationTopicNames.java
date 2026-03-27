package com.example.saga.shared.contracts.topic;

/**
 * Kafka topics used by the Saga orchestration demo.
 */
public final class OrchestrationTopicNames {
  private OrchestrationTopicNames() {
  }

  // Payment
  public static final String PAYMENT_PROCESS_COMMAND = "saga.orchestration.payment.commands.processPayment";
  public static final String PAYMENT_REFUND_COMMAND = "saga.orchestration.payment.commands.refund";
  public static final String PAYMENT_SUCCEEDED_EVENT = "saga.orchestration.payment.events.succeeded";
  public static final String PAYMENT_FAILED_EVENT = "saga.orchestration.payment.events.failed";
  public static final String PAYMENT_REFUND_COMPLETED_EVENT = "saga.orchestration.payment.events.refundCompleted";

  // Inventory
  public static final String INVENTORY_RESERVE_COMMAND = "saga.orchestration.inventory.commands.reserve";
  public static final String INVENTORY_RELEASE_COMMAND = "saga.orchestration.inventory.commands.release";
  public static final String INVENTORY_RESERVED_EVENT = "saga.orchestration.inventory.events.reserved";
  public static final String INVENTORY_FAILED_EVENT = "saga.orchestration.inventory.events.failed";
  public static final String INVENTORY_RELEASED_EVENT = "saga.orchestration.inventory.events.released";

  // Shipping
  public static final String SHIPPING_SHIP_COMMAND = "saga.orchestration.shipping.commands.ship";
  public static final String SHIPMENT_SHIPPED_EVENT = "saga.orchestration.shipping.events.shipped";
  public static final String SHIPMENT_FAILED_EVENT = "saga.orchestration.shipping.events.failed";
}

