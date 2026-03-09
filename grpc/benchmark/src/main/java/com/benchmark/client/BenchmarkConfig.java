package com.benchmark.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "benchmark")
public class BenchmarkConfig {

    private RestConfig rest = new RestConfig();
    private GrpcConfig grpc = new GrpcConfig();
    private int warmupIterations = 500;
    private int iterations = 5000;
    private int concurrentClients = 10;
    private List<String> scenarios = List.of("GET_USER", "CREATE_USER", "LIST_USERS", "BULK_CREATE");

    /** Set to "stress" to run crash/stress test instead of normal benchmark */
    private String mode = "benchmark";

    private StressConfig stress = new StressConfig();

    @Data
    public static class RestConfig {
        private String baseUrl = "http://localhost:8081";
    }

    @Data
    public static class GrpcConfig {
        private String host = "localhost";
        private int port = 9091;
    }

    @Data
    public static class StressConfig {
        private int startConcurrency = 10;
        private int maxConcurrency = 500;
        private int concurrencyStep = 20;
        private int requestsPerRound = 2000;
        private long timeoutMs = 30000;
        private double errorThresholdPercent = 30.0;
        private long cooldownMs = 1000;
    }
}
