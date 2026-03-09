package com.benchmark.client;

import com.benchmark.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.HdrHistogram.Histogram;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * gRPC benchmark client.
 */
public class GrpcBenchmarkClient {

    private final ManagedChannel channel;
    private final UserServiceGrpc.UserServiceBlockingStub blockingStub;

    public GrpcBenchmarkClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .maxInboundMessageSize(16 * 1024 * 1024)
                .build();
        this.blockingStub = UserServiceGrpc.newBlockingStub(channel);
    }

    public BenchmarkResult runBenchmark(String scenario, int warmup, int iterations, int concurrency) throws Exception {
        Runnable task = getTask(scenario);

        // Warmup phase
        System.out.printf("  [gRPC] Warming up %s (%d iterations)...%n", scenario, warmup);
        for (int i = 0; i < warmup; i++) {
            try { task.run(); } catch (Exception ignored) {}
        }

        // Benchmark phase
        System.out.printf("  [gRPC] Benchmarking %s (%d iterations, %d threads)...%n", scenario, iterations, concurrency);
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
                .protocol("gRPC")
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
        blockingStub.getUser(GetUserRequest.newBuilder().setId(id).build());
    }

    private void doCreateUser() {
        blockingStub.createUser(CreateUserRequest.newBuilder()
                .setName("Bench User")
                .setEmail("bench@test.com")
                .setAge(30)
                .setDepartment("Engineering")
                .build());
    }

    private void doListUsers() {
        blockingStub.listUsers(ListUsersRequest.newBuilder()
                .setPage(0)
                .setSize(20)
                .build());
    }

    private void doBulkCreate() {
        BulkCreateUsersRequest.Builder builder = BulkCreateUsersRequest.newBuilder();
        for (int i = 0; i < 10; i++) {
            builder.addUsers(CreateUserRequest.newBuilder()
                    .setName("Bulk User " + i)
                    .setEmail("bulk" + i + "@test.com")
                    .setAge(20 + i)
                    .setDepartment("Dept-" + (i % 5))
                    .build());
        }
        blockingStub.bulkCreateUsers(builder.build());
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
}
