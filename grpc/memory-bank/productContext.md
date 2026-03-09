# Product Context

## Why This Exists
To provide empirical performance data comparing gRPC (HTTP/2 + Protobuf) vs REST (HTTP/1.1 + JSON) APIs in a Spring Boot environment, helping developers make informed protocol choices.

## What It Measures
- **Throughput**: Requests per second under concurrent load
- **Latency**: Full distribution (min, avg, p50, p90, p95, p99, max) in microseconds
- **Reliability**: Error rates under load

## How It Works
1. Start the server (exposes identical operations via both protocols)
2. Run the benchmark client (configurable warmup, iterations, concurrency)
3. View side-by-side results with comparison metrics
