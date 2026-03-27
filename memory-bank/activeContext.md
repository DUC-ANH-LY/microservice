# Active Context

## Current focus
Implementing a complete Saga demo with both:

- Orchestration-style coordination
- Choreography-style event-driven progression

## What’s implemented
- Shared Kafka message contracts in `saga-demo/shared-contracts/`
  - DTOs for commands/events and compensation flows
  - Topic name constants for both orchestration and choreography
  - `failAt` is included in intermediate events for deterministic failures
- Orchestration demo implemented:
  - `orchestration-orchestrator` exposes `POST /sagas` and `GET /sagas/{sagaId}`
  - `orchestration-*` participant services handle step commands and emit completion events
  - Orchestrator triggers refund/inventory-release compensations when needed
- Choreography demo implemented:
  - `choreography-order-service` exposes `POST /sagas` and `GET /sagas/{sagaId}`
  - Participants react to events and publish next-step and compensation messages
  - Order service tracks state based on emitted events, including compensation completion
- Kafka runtime:
  - `saga-demo/docker-compose.yml` is added for local Kafka

## Next steps
- Finish writing documentation in `memory-bank/`
- Mark remaining todo(s) complete and ensure the project compiles cleanly.

