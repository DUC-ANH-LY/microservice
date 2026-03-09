# System Patterns

## Architecture
```
┌─────────────────────────┐     ┌─────────────────────────┐
│    Benchmark Client     │     │    Benchmark Server     │
│                         │     │                         │
│  RestBenchmarkClient ───┼─HTTP──▸ UserRestController    │
│                         │     │         │               │
│  GrpcBenchmarkClient ───┼─gRPC──▸ UserGrpcService      │
│                         │     │         │               │
│  BenchmarkApplication   │     │    UserService          │
│  (runner + reporting)   │     │    (shared logic)       │
└─────────────────────────┘     └─────────────────────────┘
```

## Key Patterns
1. **Shared Service Layer** - Both REST and gRPC call the same `UserService` for fair comparison
2. **In-Memory Store** - `ConcurrentHashMap` eliminates DB variance from benchmarks
3. **HdrHistogram** - High-precision latency recording with percentile support
4. **Concurrent Execution** - `ExecutorService` with configurable thread pool for parallel load
5. **Warmup Phase** - JIT warmup before measurement to avoid cold-start bias
