# Saga Demo (Spring Boot + Kafka)

This demo implements the Saga pattern in **two styles**:

- **Orchestration**: a central Saga orchestrator service coordinates steps and triggers compensations.
- **Choreography**: participants react to events and trigger the next step (and compensations) without a central coordinator.

The saga models this flow:

1. Payment
2. Inventory reservation
3. Shipping

Compensations:

- If **inventory reservation** fails after payment succeeds: issue a **refund**
- If **shipping** fails after payment + inventory succeeded: **release inventory** and **refund**

## Requirements

- Java 17+
- Maven
- Docker (to run Kafka)

## 1) Start Kafka

```bash
cd /home/ducanh/Downloads/saga/saga-demo
docker compose up -d
```

Kafka will be available at `localhost:9092`.
Kafka UI will be available at `http://localhost:8088`.

## 2) Run the Orchestration demo

Orchestrator REST API:

- `POST /sagas` (start)
- `GET /sagas/{sagaId}` (status)

Port: `8081`

Start these services in separate terminals (from `saga-demo/`):

```bash
mvn -pl orchestration-orchestrator spring-boot:run
```

```bash
mvn -pl orchestration-payment-service spring-boot:run
```

```bash
mvn -pl orchestration-inventory-service spring-boot:run
```

```bash
mvn -pl orchestration-shipping-service spring-boot:run
```

### Trigger an orchestration saga

```bash
curl -X POST http://localhost:8081/sagas \
  -H 'Content-Type: application/json' \
  -d '{"orderId":"A1","failAt":"INVENTORY"}'
```

Then poll status (replace `<sagaId>` with the returned id):

```bash
curl http://localhost:8081/sagas/<sagaId>
```

Use `failAt` values: `PAYMENT`, `INVENTORY`, `SHIPMENT`.

## 3) Run the Choreography demo

Order service REST API:

- `POST /sagas` (start)
- `GET /sagas/{sagaId}` (status)

Port: `8082`

Start these services in separate terminals (from `saga-demo/`):

```bash
mvn -pl choreography-order-service spring-boot:run
```

```bash
mvn -pl choreography-payment-service spring-boot:run
```

```bash
mvn -pl choreography-inventory-service spring-boot:run
```

```bash
mvn -pl choreography-shipping-service spring-boot:run
```

### Trigger a choreography saga

```bash
curl -X POST http://localhost:8082/sagas \
  -H 'Content-Type: application/json' \
  -d '{"orderId":"B1","failAt":"SHIPMENT"}'
```

Then poll status:

```bash
curl http://localhost:8082/sagas/<sagaId>
```

## Notes

- Kafka topics are auto-created by the broker (`KAFKA_AUTO_CREATE_TOPICS_ENABLE=true`).
- All services connect to Kafka via `KAFKA_BOOTSTRAP_SERVERS` (defaults to `localhost:9092`).
- Use Kafka UI at `http://localhost:8088` to browse topics, inspect messages, and view consumer groups/lag.

## Sequence Diagrams

### Orchestration

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant O as Orchestrator API (8081)
    participant K as Kafka
    participant P as Payment Service
    participant I as Inventory Service
    participant S as Shipping Service

    User->>O: POST /sagas {orderId, failAt}
    O->>O: Create sagaId + in-memory state
    O->>K: Publish ProcessPaymentCommand

    K-->>P: ProcessPaymentCommand
    alt failAt == PAYMENT
        P->>K: PaymentFailedEvent
        K-->>O: PaymentFailedEvent
        O->>O: Mark saga FAILED
    else Payment success
        P->>K: PaymentSucceededEvent
        K-->>O: PaymentSucceededEvent
        O->>K: Publish ReserveInventoryCommand

        K-->>I: ReserveInventoryCommand
        alt failAt == INVENTORY
            I->>K: InventoryReserveFailedEvent
            K-->>O: InventoryReserveFailedEvent
            O->>K: Publish RefundCommand
            K-->>P: RefundCommand
            P->>K: RefundCompletedEvent
            K-->>O: RefundCompletedEvent
            O->>O: Mark saga FAILED (compensated)
        else Inventory success
            I->>K: InventoryReservedEvent
            K-->>O: InventoryReservedEvent
            O->>K: Publish ShipOrderCommand

            K-->>S: ShipOrderCommand
            alt failAt == SHIPMENT
                S->>K: ShipmentFailedEvent
                K-->>O: ShipmentFailedEvent
                O->>K: Publish ReleaseInventoryCommand
                O->>K: Publish RefundCommand

                K-->>I: ReleaseInventoryCommand
                I->>K: InventoryReleasedEvent
                K-->>O: InventoryReleasedEvent

                K-->>P: RefundCommand
                P->>K: RefundCompletedEvent
                K-->>O: RefundCompletedEvent

                O->>O: Mark saga FAILED (compensated)
            else Shipment success
                S->>K: ShipmentShippedEvent
                K-->>O: ShipmentShippedEvent
                O->>O: Mark saga COMPLETED
            end
        end
    end

    User->>O: GET /sagas/{sagaId}
    O-->>User: status + step states
```

### Choreography

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant O as Order Service API (8082)
    participant K as Kafka
    participant P as Payment Service
    participant I as Inventory Service
    participant S as Shipping Service

    User->>O: POST /sagas {orderId, failAt}
    O->>O: Create sagaId + in-memory tracker
    O->>K: Publish OrderCreatedEvent

    K-->>P: OrderCreatedEvent
    alt failAt == PAYMENT
        P->>K: PaymentFailedEvent
        K-->>O: PaymentFailedEvent
        O->>O: Mark saga FAILED
    else Payment success
        P->>K: PaymentSucceededEvent
        K-->>I: PaymentSucceededEvent
        K-->>O: PaymentSucceededEvent

        alt failAt == INVENTORY
            I->>K: InventoryReserveFailedEvent
            I->>K: RefundCommand
            K-->>P: RefundCommand
            P->>K: RefundCompletedEvent

            K-->>O: InventoryReserveFailedEvent
            K-->>O: RefundCompletedEvent
            O->>O: Mark saga FAILED (compensated)
        else Inventory success
            I->>K: InventoryReservedEvent
            K-->>S: InventoryReservedEvent
            K-->>O: InventoryReservedEvent

            alt failAt == SHIPMENT
                S->>K: ShipmentFailedEvent
                S->>K: ReleaseInventoryCommand
                S->>K: RefundCommand

                K-->>I: ReleaseInventoryCommand
                I->>K: InventoryReleasedEvent
                K-->>P: RefundCommand
                P->>K: RefundCompletedEvent

                K-->>O: ShipmentFailedEvent
                K-->>O: InventoryReleasedEvent
                K-->>O: RefundCompletedEvent
                O->>O: Mark saga FAILED (compensated)
            else Shipment success
                S->>K: ShipmentShippedEvent
                K-->>O: ShipmentShippedEvent
                O->>O: Mark saga COMPLETED
            end
        end
    end

    User->>O: GET /sagas/{sagaId}
    O-->>User: status + step states
```

