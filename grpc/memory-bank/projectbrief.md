# Project Brief: gRPC vs REST Benchmark

## Overview
A Spring Boot multi-module Maven project that benchmarks gRPC against REST API performance using identical service operations.

## Core Requirements
- Server exposing both REST (HTTP/JSON) and gRPC (Protobuf) endpoints for the same operations
- Benchmark client that measures throughput and latency for both protocols
- Fair comparison using the same service layer, data model, and operations
- Configurable benchmark parameters (iterations, concurrency, scenarios)

## Benchmark Scenarios
1. **GET_USER** - Simple read, small payload
2. **CREATE_USER** - Single write, medium payload
3. **LIST_USERS** - Paginated list, larger response
4. **BULK_CREATE** - Batch write, large request payload

## Success Metrics
- Accurate latency percentiles (p50, p90, p95, p99)
- Throughput measurement (requests/second)
- Clear comparison output between protocols
