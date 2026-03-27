# Project Brief: Saga Demo (Spring Boot + Kafka)

## Purpose
Provide a runnable, educational demo of the **Saga pattern** implemented in Spring Boot using **Kafka**. The demo includes both:

- **Orchestration**: a central Saga orchestrator coordinates steps and triggers compensations.
- **Choreography**: participants react to domain events and trigger subsequent steps and compensations without a central coordinator.

## Core Saga Flow
1. Payment
2. Reserve Inventory
3. Ship Order

## Compensation Rules
- If inventory reservation fails after payment succeeded: trigger **Refund**
- If shipping fails after payment + inventory succeeded: trigger **Release Inventory** and **Refund**

## Deliverables
- Multi-module Spring Boot project (`saga-demo/`)
- Shared Kafka contracts module (`shared-contracts/`)
- Orchestration demo services:
  - `orchestration-orchestrator` (REST API + saga state)
  - `orchestration-payment-service`
  - `orchestration-inventory-service`
  - `orchestration-shipping-service`
- Choreography demo services:
  - `choreography-order-service` (REST API + saga state)
  - `choreography-payment-service`
  - `choreography-inventory-service`
  - `choreography-shipping-service`
- Kafka local runtime via `docker-compose.yml`
- Runbook `README.md`

