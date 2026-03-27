# Product Context

## What problem does this solve?
Many teams want to learn or validate Saga implementations, but real microservice setups are heavy to stand up. This demo provides a small, runnable reference for:

- Coordinating multi-step business operations
- Handling failures with compensating actions
- Comparing Saga **orchestration** vs **choreography**

## User experience goals
- One command flow to start Kafka locally
- Consistent REST API in both demos:
  - `POST /sagas` to start
  - `GET /sagas/{sagaId}` to observe progress
- Deterministic failure injection via `failAt` so users can quickly see compensation.

## How it should work (mental model)
Each saga instance carries:
- `sagaId` for correlation
- `orderId` for business context
- `correlationId` for message grouping
- `failAt` to deterministically force failures at a specific step

In orchestration, the orchestrator decides next steps and compensation.
In choreography, participants react to events and publish next-step/compensation messages.

