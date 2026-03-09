package com.benchmark.client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Benchmark runner that compares gRPC vs REST API performance.
 *
 * Usage:
 *   1. Start the server module first (port 8080 for REST, 9090 for gRPC)
 *   2. Run this application to execute benchmarks
 *
 * Configuration can be changed in application.yml or via command-line args:
 *   --benchmark.iterations=10000
 *   --benchmark.concurrent-clients=20
 *   --benchmark.scenarios=GET_USER,CREATE_USER
 */
@SpringBootApplication
public class BenchmarkApplication implements CommandLineRunner {

    private final BenchmarkConfig config;

    public BenchmarkApplication(BenchmarkConfig config) {
        this.config = config;
    }

    public static void main(String[] args) {
        SpringApplication.run(BenchmarkApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if ("stress".equalsIgnoreCase(config.getMode())) {
            // Crash / Stress test mode
            StressTestRunner stressRunner = new StressTestRunner(config);
            stressRunner.run();
            return;
        }

        // Normal benchmark mode
        printBanner();

        RestBenchmarkClient restClient = new RestBenchmarkClient(config.getRest().getBaseUrl());
        GrpcBenchmarkClient grpcClient = new GrpcBenchmarkClient(
                config.getGrpc().getHost(),
                config.getGrpc().getPort()
        );

        List<BenchmarkResult> allResults = new ArrayList<>();

        for (String scenario : config.getScenarios()) {
            System.out.println("\n" + "═".repeat(60));
            System.out.printf("  SCENARIO: %s%n", scenario);
            System.out.println("═".repeat(60));

            // Run REST benchmark
            BenchmarkResult restResult = restClient.runBenchmark(
                    scenario,
                    config.getWarmupIterations(),
                    config.getIterations(),
                    config.getConcurrentClients()
            );
            allResults.add(restResult);
            restResult.print();

            // Run gRPC benchmark
            BenchmarkResult grpcResult = grpcClient.runBenchmark(
                    scenario,
                    config.getWarmupIterations(),
                    config.getIterations(),
                    config.getConcurrentClients()
            );
            allResults.add(grpcResult);
            grpcResult.print();

            // Print comparison
            printComparison(restResult, grpcResult);
        }

        grpcClient.shutdown();

        // Print summary
        printSummary(allResults);
    }

    private void printComparison(BenchmarkResult rest, BenchmarkResult grpc) {
        System.out.println();
        System.out.printf("  ▸ Throughput: gRPC is %.2fx %s than REST%n",
                Math.max(grpc.getRequestsPerSecond(), rest.getRequestsPerSecond()) /
                        Math.min(grpc.getRequestsPerSecond(), rest.getRequestsPerSecond()),
                grpc.getRequestsPerSecond() > rest.getRequestsPerSecond() ? "faster" : "slower");
        System.out.printf("  ▸ Avg Latency: gRPC is %.2fx %s than REST%n",
                Math.max(grpc.getAvgLatencyUs(), rest.getAvgLatencyUs()) /
                        Math.min(grpc.getAvgLatencyUs(), rest.getAvgLatencyUs()),
                grpc.getAvgLatencyUs() < rest.getAvgLatencyUs() ? "faster" : "slower");
        System.out.printf("  ▸ P99 Latency: gRPC is %.2fx %s than REST%n",
                Math.max(grpc.getP99LatencyUs(), rest.getP99LatencyUs()) /
                        Math.min(grpc.getP99LatencyUs(), rest.getP99LatencyUs()),
                grpc.getP99LatencyUs() < rest.getP99LatencyUs() ? "faster" : "slower");
    }

    private void printSummary(List<BenchmarkResult> results) {
        System.out.println("\n\n");
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                              BENCHMARK SUMMARY                                     ║");
        System.out.println("╠══════════════╦════════════╦════════════╦════════════╦════════════╦═════════════════╣");
        System.out.println("║  Scenario    ║ Protocol   ║ Throughput ║ Avg (μs)   ║ P99 (μs)   ║ Errors          ║");
        System.out.println("╠══════════════╬════════════╬════════════╬════════════╬════════════╬═════════════════╣");

        for (BenchmarkResult r : results) {
            System.out.printf("║ %-12s ║ %-10s ║ %8.1f   ║ %8.1f   ║ %8.1f   ║ %6d / %-6d ║%n",
                    truncate(r.getScenario(), 12),
                    r.getProtocol(),
                    r.getRequestsPerSecond(),
                    r.getAvgLatencyUs(),
                    r.getP99LatencyUs(),
                    r.getErrorCount(),
                    r.getTotalRequests());
        }

        System.out.println("╚══════════════╩════════════╩════════════╩════════════╩════════════╩═════════════════╝");

        System.out.println("\nBenchmark configuration:");
        System.out.printf("  Warmup iterations  : %d%n", results.isEmpty() ? 0 : results.get(0).getTotalRequests());
        System.out.printf("  Test iterations    : %d%n", results.isEmpty() ? 0 : results.get(0).getTotalRequests());
        System.out.printf("  Concurrent clients : configured in application.yml%n");
        System.out.println();
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) : s;
    }

    private void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║         gRPC vs REST Benchmark Suite                ║");
        System.out.println("║                                                     ║");
        System.out.println("║  Scenarios: GET_USER, CREATE_USER,                  ║");
        System.out.println("║             LIST_USERS, BULK_CREATE                 ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.printf("%n  Warmup iterations  : %d%n", config.getWarmupIterations());
        System.out.printf("  Test iterations    : %d%n", config.getIterations());
        System.out.printf("  Concurrent clients : %d%n", config.getConcurrentClients());
        System.out.printf("  REST base URL      : %s%n", config.getRest().getBaseUrl());
        System.out.printf("  gRPC target        : %s:%d%n", config.getGrpc().getHost(), config.getGrpc().getPort());
        System.out.println();
    }
}
