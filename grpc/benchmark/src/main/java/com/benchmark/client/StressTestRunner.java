package com.benchmark.client;

import org.HdrHistogram.Histogram;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import com.benchmark.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Stress/Crash test runner that ramps up load until the server breaks.
 *
 * Strategy:
 *   1. Start with a small number of concurrent clients
 *   2. Each round increases concurrency (ramp-up)
 *   3. Track error rates, latency spikes, timeouts
 *   4. Stop when error rate exceeds threshold or server becomes unresponsive
 */
public class StressTestRunner {

    private final BenchmarkConfig config;

    public StressTestRunner(BenchmarkConfig config) {
        this.config = config;
    }

    public void run() throws Exception {
        BenchmarkConfig.StressConfig stress = config.getStress();

        printStressBanner(stress);

        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("  STRESS TEST: REST API");
        System.out.println("══════════════════════════════════════════════════════════");
        List<StressRoundResult> restResults = runStressTest("REST", stress);

        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("  STRESS TEST: gRPC API");
        System.out.println("══════════════════════════════════════════════════════════");
        List<StressRoundResult> grpcResults = runStressTest("gRPC", stress);

        printStressSummary(restResults, grpcResults);
    }

    private List<StressRoundResult> runStressTest(String protocol, BenchmarkConfig.StressConfig stress) throws Exception {
        List<StressRoundResult> results = new ArrayList<>();
        int concurrency = stress.getStartConcurrency();

        while (concurrency <= stress.getMaxConcurrency()) {
            System.out.printf("%n  ── Round: %d concurrent clients, %d requests ──%n", concurrency, stress.getRequestsPerRound());

            StressRoundResult result;
            if ("REST".equals(protocol)) {
                result = runRestRound(concurrency, stress.getRequestsPerRound(), stress.getTimeoutMs());
            } else {
                result = runGrpcRound(concurrency, stress.getRequestsPerRound(), stress.getTimeoutMs());
            }

            result.setConcurrency(concurrency);
            result.setProtocol(protocol);
            results.add(result);
            printRoundResult(result, stress.getErrorThresholdPercent());

            // Check if server is broken
            if (result.getErrorPercent() >= stress.getErrorThresholdPercent()) {
                System.out.printf("%n  🔥 SERVER BREAKING POINT REACHED at %d concurrent clients!%n", concurrency);
                System.out.printf("     Error rate: %.1f%% (threshold: %.1f%%)%n",
                        result.getErrorPercent(), stress.getErrorThresholdPercent());
                break;
            }

            if (result.isTimedOut()) {
                System.out.printf("%n  💀 SERVER UNRESPONSIVE at %d concurrent clients!%n", concurrency);
                break;
            }

            concurrency += stress.getConcurrencyStep();

            // Brief pause between rounds to let the server recover
            Thread.sleep(stress.getCooldownMs());
        }

        return results;
    }

    // ── REST stress round ──

    private StressRoundResult runRestRound(int concurrency, int totalRequests, long timeoutMs) {
        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = config.getRest().getBaseUrl();

        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        Histogram histogram = new Histogram(3_600_000_000L, 3);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicInteger timeoutCount = new AtomicInteger(0);
        AtomicBoolean timedOut = new AtomicBoolean(false);

        long startTime = System.nanoTime();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            final int reqId = i;
            futures.add(executor.submit(() -> {
                long start = System.nanoTime();
                try {
                    // Mix of operations to simulate real traffic
                    switch (reqId % 4) {
                        case 0 -> restTemplate.getForObject(
                                baseUrl + "/api/users/" + (ThreadLocalRandom.current().nextLong(1, 101)), String.class);
                        case 1 -> {
                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            String body = """
                                    {"name":"Stress User","email":"stress@test.com","age":25,"department":"QA"}""";
                            restTemplate.postForObject(baseUrl + "/api/users", new HttpEntity<>(body, headers), String.class);
                        }
                        case 2 -> restTemplate.getForObject(baseUrl + "/api/users?page=0&size=50", String.class);
                        case 3 -> {
                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            StringBuilder sb = new StringBuilder("[");
                            for (int j = 0; j < 20; j++) {
                                if (j > 0) sb.append(",");
                                sb.append(String.format(
                                        """
                                        {"name":"Bulk %d","email":"b%d@test.com","age":%d,"department":"D%d"}""",
                                        j, j, 20 + j, j % 5));
                            }
                            sb.append("]");
                            restTemplate.postForObject(baseUrl + "/api/users/bulk", new HttpEntity<>(sb.toString(), headers), String.class);
                        }
                    }
                    long elapsed = (System.nanoTime() - start) / 1000;
                    synchronized (histogram) {
                        histogram.recordValue(Math.max(1, elapsed));
                    }
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                        timeoutCount.incrementAndGet();
                    }
                    errorCount.incrementAndGet();
                }
            }));
        }

        // Wait with overall timeout
        try {
            executor.shutdown();
            if (!executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
                timedOut.set(true);
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            timedOut.set(true);
        }

        long totalDuration = (System.nanoTime() - startTime) / 1_000_000;

        return buildRoundResult(histogram, successCount.get(), errorCount.get(),
                timeoutCount.get(), totalDuration, totalRequests, timedOut.get());
    }

    // ── gRPC stress round ──

    private StressRoundResult runGrpcRound(int concurrency, int totalRequests, long timeoutMs) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(config.getGrpc().getHost(), config.getGrpc().getPort())
                .usePlaintext()
                .maxInboundMessageSize(16 * 1024 * 1024)
                .build();
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);

        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        Histogram histogram = new Histogram(3_600_000_000L, 3);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicInteger timeoutCount = new AtomicInteger(0);
        AtomicBoolean timedOut = new AtomicBoolean(false);

        long startTime = System.nanoTime();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            final int reqId = i;
            futures.add(executor.submit(() -> {
                long start = System.nanoTime();
                try {
                    switch (reqId % 4) {
                        case 0 -> stub.getUser(GetUserRequest.newBuilder()
                                .setId(ThreadLocalRandom.current().nextLong(1, 101)).build());
                        case 1 -> stub.createUser(CreateUserRequest.newBuilder()
                                .setName("Stress User").setEmail("stress@test.com")
                                .setAge(25).setDepartment("QA").build());
                        case 2 -> stub.listUsers(ListUsersRequest.newBuilder()
                                .setPage(0).setSize(50).build());
                        case 3 -> {
                            BulkCreateUsersRequest.Builder builder = BulkCreateUsersRequest.newBuilder();
                            for (int j = 0; j < 20; j++) {
                                builder.addUsers(CreateUserRequest.newBuilder()
                                        .setName("Bulk " + j).setEmail("b" + j + "@test.com")
                                        .setAge(20 + j).setDepartment("D" + (j % 5)).build());
                            }
                            stub.bulkCreateUsers(builder.build());
                        }
                    }
                    long elapsed = (System.nanoTime() - start) / 1000;
                    synchronized (histogram) {
                        histogram.recordValue(Math.max(1, elapsed));
                    }
                    successCount.incrementAndGet();
                } catch (StatusRuntimeException e) {
                    if (e.getStatus().getCode() == io.grpc.Status.Code.DEADLINE_EXCEEDED) {
                        timeoutCount.incrementAndGet();
                    }
                    errorCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                }
            }));
        }

        try {
            executor.shutdown();
            if (!executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
                timedOut.set(true);
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            timedOut.set(true);
        }

        long totalDuration = (System.nanoTime() - startTime) / 1_000_000;

        try { channel.shutdown().awaitTermination(2, TimeUnit.SECONDS); } catch (Exception ignored) {}

        return buildRoundResult(histogram, successCount.get(), errorCount.get(),
                timeoutCount.get(), totalDuration, totalRequests, timedOut.get());
    }

    // ── Helpers ──

    private StressRoundResult buildRoundResult(Histogram histogram, int success, int errors,
                                                int timeouts, long durationMs, int total, boolean timedOut) {
        StressRoundResult result = new StressRoundResult();
        result.setTotalRequests(total);
        result.setSuccessCount(success);
        result.setErrorCount(errors);
        result.setTimeoutCount(timeouts);
        result.setTotalDurationMs(durationMs);
        result.setTimedOut(timedOut);

        if (histogram.getTotalCount() > 0) {
            result.setAvgLatencyUs(histogram.getMean());
            result.setP50LatencyUs(histogram.getValueAtPercentile(50));
            result.setP99LatencyUs(histogram.getValueAtPercentile(99));
            result.setMaxLatencyUs(histogram.getMaxValue());
        }

        if (durationMs > 0) {
            result.setRequestsPerSecond(success / (durationMs / 1000.0));
        }

        result.setErrorPercent(total > 0 ? (errors * 100.0 / total) : 0);
        return result;
    }

    private void printRoundResult(StressRoundResult r, double threshold) {
        String status;
        if (r.isTimedOut()) {
            status = "💀 TIMEOUT";
        } else if (r.getErrorPercent() >= threshold) {
            status = "🔥 BREAKING";
        } else if (r.getErrorPercent() > 0) {
            status = "⚠️  DEGRADED";
        } else {
            status = "✅ HEALTHY";
        }

        System.out.printf("  %s │ %4d clients │ %6.0f rps │ avg %8.0f μs │ p99 %8.0f μs │ err %5.1f%% │ %s%n",
                r.getProtocol(),
                r.getConcurrency(),
                r.getRequestsPerSecond(),
                r.getAvgLatencyUs(),
                r.getP99LatencyUs(),
                r.getErrorPercent(),
                status);
    }

    private void printStressSummary(List<StressRoundResult> restResults, List<StressRoundResult> grpcResults) {
        System.out.println("\n\n");
        System.out.println("╔════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                           STRESS TEST SUMMARY                                        ║");
        System.out.println("╠══════════╦══════════╦══════════════╦════════════╦════════════╦════════════╦═══════════╣");
        System.out.println("║ Protocol ║ Clients  ║ Throughput   ║  Avg (μs)  ║  P99 (μs)  ║  Errors    ║  Status   ║");
        System.out.println("╠══════════╬══════════╬══════════════╬════════════╬════════════╬════════════╬═══════════╣");

        double threshold = config.getStress().getErrorThresholdPercent();
        for (StressRoundResult r : restResults) {
            printSummaryRow(r, threshold);
        }
        System.out.println("╠══════════╬══════════╬══════════════╬════════════╬════════════╬════════════╬═══════════╣");
        for (StressRoundResult r : grpcResults) {
            printSummaryRow(r, threshold);
        }

        System.out.println("╚══════════╩══════════╩══════════════╩════════════╩════════════╩════════════╩═══════════╝");

        // Find breaking points
        int restBreak = findBreakingPoint(restResults, threshold);
        int grpcBreak = findBreakingPoint(grpcResults, threshold);

        System.out.println();
        System.out.println("  ┌─────────────────────────────────────────────┐");
        System.out.printf("  │  REST breaking point : %-20s │%n",
                restBreak > 0 ? restBreak + " concurrent clients" : "NOT REACHED ✅");
        System.out.printf("  │  gRPC breaking point : %-20s │%n",
                grpcBreak > 0 ? grpcBreak + " concurrent clients" : "NOT REACHED ✅");
        if (restBreak > 0 && grpcBreak > 0) {
            System.out.printf("  │  gRPC survives %.1fx more load than REST    │%n",
                    (double) grpcBreak / restBreak);
        }
        System.out.println("  └─────────────────────────────────────────────┘");

        // Find peak throughput
        double restPeak = restResults.stream().mapToDouble(StressRoundResult::getRequestsPerSecond).max().orElse(0);
        double grpcPeak = grpcResults.stream().mapToDouble(StressRoundResult::getRequestsPerSecond).max().orElse(0);
        System.out.printf("%n  Peak throughput: REST=%.0f rps, gRPC=%.0f rps%n", restPeak, grpcPeak);
        System.out.println();
    }

    private void printSummaryRow(StressRoundResult r, double threshold) {
        String status = r.isTimedOut() ? "TIMEOUT" :
                r.getErrorPercent() >= threshold ? "BROKEN" :
                        r.getErrorPercent() > 0 ? "DEGRADED" : "OK";
        System.out.printf("║ %-8s ║ %6d   ║ %10.0f   ║ %8.0f   ║ %8.0f   ║ %8.1f%%  ║ %-9s ║%n",
                r.getProtocol(), r.getConcurrency(), r.getRequestsPerSecond(),
                r.getAvgLatencyUs(), r.getP99LatencyUs(), r.getErrorPercent(), status);
    }

    private int findBreakingPoint(List<StressRoundResult> results, double threshold) {
        for (StressRoundResult r : results) {
            if (r.getErrorPercent() >= threshold || r.isTimedOut()) {
                return r.getConcurrency();
            }
        }
        return -1;
    }

    private void printStressBanner(BenchmarkConfig.StressConfig stress) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║        🔥 CRASH / STRESS TEST MODE 🔥              ║");
        System.out.println("║                                                     ║");
        System.out.println("║  Ramp up concurrency until the server breaks!       ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.printf("%n  Start concurrency  : %d%n", stress.getStartConcurrency());
        System.out.printf("  Max concurrency    : %d%n", stress.getMaxConcurrency());
        System.out.printf("  Step               : +%d per round%n", stress.getConcurrencyStep());
        System.out.printf("  Requests per round : %d%n", stress.getRequestsPerRound());
        System.out.printf("  Round timeout      : %d ms%n", stress.getTimeoutMs());
        System.out.printf("  Error threshold    : %.0f%%%n", stress.getErrorThresholdPercent());
        System.out.printf("  Cooldown           : %d ms%n", stress.getCooldownMs());
    }

    // ── Inner result class ──

    @lombok.Data
    public static class StressRoundResult {
        private String protocol;
        private int concurrency;
        private int totalRequests;
        private int successCount;
        private int errorCount;
        private int timeoutCount;
        private long totalDurationMs;
        private boolean timedOut;
        private double avgLatencyUs;
        private double p50LatencyUs;
        private double p99LatencyUs;
        private double maxLatencyUs;
        private double requestsPerSecond;
        private double errorPercent;
    }
}
