# System Patterns

## Architecture overview

### Orchestration flow
Central coordinator (`orchestration-orchestrator`) drives the saga state machine:
- Starts with `ProcessPaymentCommand`
- On `PaymentSucceededEvent` -> sends `ReserveInventoryCommand`
- On `InventoryReservedEvent` -> sends `ShipOrderCommand`
- On failures after payment/inventory:
  - Inventory failure -> sends `RefundCommand`
  - Shipping failure -> sends `ReleaseInventoryCommand` and `RefundCommand`
- Marks saga complete/failed only after receiving the required completion events

### Choreography flow
No central coordinator. The participants react to each other’s messages:
- `choreography-order-service` publishes `OrderCreatedEvent`
- `choreography-payment-service` reacts with payment success/failure
- `choreography-inventory-service` reacts to payment success and emits reservation success/failure
- On inventory failure, it publishes a `RefundCommand` compensation trigger
- `choreography-shipping-service` reacts to inventory reservation success
- On shipping failure, it publishes both `ReleaseInventoryCommand` and `RefundCommand` compensation triggers
- `choreography-order-service` tracks status based on emitted events, including compensation completions

## Reliability considerations (demo scope)
- Uses in-memory saga state (sufficient for a demo)
- Kafka topics are auto-created (broker config)
- No retry/outbox patterns are implemented here (kept intentionally minimal for learning)

