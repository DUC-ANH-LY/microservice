package com.benchmark.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.HdrHistogram.Histogram;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * REST API benchmark client.
 */
public class RestBenchmarkClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public RestBenchmarkClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public BenchmarkResult runBenchmark(String scenario, int warmup, int iterations, int concurrency) throws Exception {
        Runnable task = getTask(scenario);

        // Warmup phase
        System.out.printf("  [REST] Warming up %s (%d iterations)...%n", scenario, warmup);
        for (int i = 0; i < warmup; i++) {
            try { task.run(); } catch (Exception ignored) {}
        }

        // Benchmark phase
        System.out.printf("  [REST] Benchmarking %s (%d iterations, %d threads)...%n", scenario, iterations, concurrency);
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        Histogram histogram = new Histogram(3_600_000_000L, 3); // up to 1 hour in μs
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        long startTime = System.nanoTime();

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            futures.add(executor.submit(() -> {
                long start = System.nanoTime();
                try {
                    task.run();
                    long elapsed = (System.nanoTime() - start) / 1000; // convert to μs
                    synchronized (histogram) {
                        histogram.recordValue(Math.max(1, elapsed));
                    }
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                }
            }));
        }

        for (Future<?> f : futures) {
            f.get(30, TimeUnit.SECONDS);
        }

        long totalDuration = (System.nanoTime() - startTime) / 1_000_000; // ms
        executor.shutdown();

        return BenchmarkResult.builder()
                .protocol("REST")
                .scenario(scenario)
                .totalRequests(iterations)
                .successCount(successCount.get())
                .errorCount(errorCount.get())
                .totalDurationMs(totalDuration)
                .avgLatencyUs(histogram.getMean())
                .minLatencyUs(histogram.getMinValue())
                .p50LatencyUs(histogram.getValueAtPercentile(50))
                .p90LatencyUs(histogram.getValueAtPercentile(90))
                .p95LatencyUs(histogram.getValueAtPercentile(95))
                .p99LatencyUs(histogram.getValueAtPercentile(99))
                .maxLatencyUs(histogram.getMaxValue())
                .requestsPerSecond(successCount.get() / (totalDuration / 1000.0))
                .build();
    }

    private Runnable getTask(String scenario) {
        return switch (scenario) {
            case "GET_USER" -> this::doGetUser;
            case "CREATE_USER" -> this::doCreateUser;
            case "LIST_USERS" -> this::doListUsers;
            case "BULK_CREATE" -> this::doBulkCreate;
            default -> throw new IllegalArgumentException("Unknown scenario: " + scenario);
        };
    }

    private void doGetUser() {
        long id = ThreadLocalRandom.current().nextLong(1, 101);
        restTemplate.getForObject(baseUrl + "/api/users/" + id, String.class);
    }

    private void doCreateUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"name":"Bench User","email":"bench@test.com","age":30,"department":"Engineering"}
                """;
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        restTemplate.postForObject(baseUrl + "/api/users", entity, String.class);
    }

    private void doListUsers() {
        restTemplate.getForObject(baseUrl + "/api/users?page=0&size=20", String.class);
    }

    private void doBulkCreate() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 10; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format(
                    """
                    {"name":"Bulk User %d","email":"bulk%d@test.com","age":%d,"department":"Dept-%d"}
                    """, i, i, 20 + i, i % 5));
        }
        sb.append("]");
        HttpEntity<String> entity = new HttpEntity<>(sb.toString(), headers);
        restTemplate.postForObject(baseUrl + "/api/users/bulk", entity, String.class);
    }
}
