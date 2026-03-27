# Progress

## What works
- Maven multi-module project skeleton created under `saga-demo/`
- Shared Kafka contracts implemented:
  - DTOs for commands/events/compensations
  - Topic names for both saga styles
  - `failAt` included in intermediate messages for deterministic failures
- Orchestration demo implemented:
  - `orchestration-orchestrator` provides `POST /sagas` and `GET /sagas/{sagaId}`
  - Compensation is triggered and saga completes/fails based on completion events
- Choreography demo implemented:
  - `choreography-order-service` provides `POST /sagas` and `GET /sagas/{sagaId}`
  - Participants react to events and trigger compensations on failures
- Local Kafka runtime:
  - `saga-demo/docker-compose.yml` added
- Runbook added:
  - `saga-demo/README.md` documents how to run and test both demos

## Known gaps / follow-ups (optional)
- There are no automated tests in this demo.
- Topics are auto-created; for production-like setups you might prefer explicit topic provisioning.
- No persistence layer for saga state (in-memory only).

