package com.benchmark.client;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BenchmarkResult {

    private String protocol;       // "REST" or "gRPC"
    private String scenario;       // e.g. "GET_USER"
    private int totalRequests;
    private int successCount;
    private int errorCount;
    private long totalDurationMs;

    // Latency metrics in microseconds
    private double avgLatencyUs;
    private double p50LatencyUs;
    private double p90LatencyUs;
    private double p95LatencyUs;
    private double p99LatencyUs;
    private double maxLatencyUs;
    private double minLatencyUs;

    // Throughput
    private double requestsPerSecond;

    public void print() {
        System.out.printf("┌─────────────────────────────────────────────────────────┐%n");
        System.out.printf("│  %-8s │ %-42s │%n", protocol, scenario);
        System.out.printf("├─────────────────────────────────────────────────────────┤%n");
        System.out.printf("│  Total Requests    : %-35d │%n", totalRequests);
        System.out.printf("│  Success           : %-35d │%n", successCount);
        System.out.printf("│  Errors            : %-35d │%n", errorCount);
        System.out.printf("│  Duration          : %-32d ms │%n", totalDurationMs);
        System.out.printf("│  Throughput        : %-30.2f rps │%n", requestsPerSecond);
        System.out.printf("├─────────────────────────────────────────────────────────┤%n");
        System.out.printf("│  Avg Latency       : %-30.2f μs  │%n", avgLatencyUs);
        System.out.printf("│  Min Latency       : %-30.2f μs  │%n", minLatencyUs);
        System.out.printf("│  P50 Latency       : %-30.2f μs  │%n", p50LatencyUs);
        System.out.printf("│  P90 Latency       : %-30.2f μs  │%n", p90LatencyUs);
        System.out.printf("│  P95 Latency       : %-30.2f μs  │%n", p95LatencyUs);
        System.out.printf("│  P99 Latency       : %-30.2f μs  │%n", p99LatencyUs);
        System.out.printf("│  Max Latency       : %-30.2f μs  │%n", maxLatencyUs);
        System.out.printf("└─────────────────────────────────────────────────────────┘%n");
    }
}
