# Tech Context

## Stack
- Spring Boot (Java 17)
- Spring for Apache Kafka (`spring-kafka`)
- Maven multi-module build
- Kafka + Zookeeper (local) via `docker-compose.yml`

## Messaging/serialization
- Shared DTOs live in `saga-demo/shared-contracts`
- Kafka uses JSON serialization:
  - Producer: `JsonSerializer` with type headers enabled
  - Consumer: `JsonDeserializer` trusting the `com.example.saga.shared.contracts` package
- `failAt` is included in intermediate success/failure events to support deterministic failure injection.

## Module mapping
- `orchestration-orchestrator`: REST API + saga state machine + Kafka consumer(s)
- `orchestration-payment-service`: consumes payment commands and refund commands
- `orchestration-inventory-service`: consumes inventory reserve and release commands
- `orchestration-shipping-service`: consumes ship commands
- `choreography-order-service`: REST API + saga state tracker (consumes all relevant events)
- `choreography-payment-service`: consumes `OrderCreatedEvent` + `RefundCommand`
- `choreography-inventory-service`: consumes `PaymentSucceededEvent` + `ReleaseInventoryCommand`
- `choreography-shipping-service`: consumes `InventoryReservedEvent`

