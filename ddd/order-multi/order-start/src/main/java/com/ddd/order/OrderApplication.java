package com.ddd.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DDD Order Demo — Multi Module.
 *
 * Spring Boot entry point. The component scan at package com.ddd.order
 * automatically discovers all beans across all 7 Maven modules:
 *   order-shared   → marker annotations (no beans)
 *   order-domain   → pure Java, no beans
 *   order-application → @Service OrderCmdServiceImpl
 *   order-infrastructure → @Repository OrderRepositoryImpl, @Configuration DomainConfig
 *   order-query    → @Service OrderQueryService
 *   order-api      → @RestController OrderCommandController, OrderQueryController
 *   order-start    → this class
 *
 * Reference structure: https://github.com/Sayi/ddd-cargo (ddd-cargo-maven-module-example)
 *
 * Run: mvn spring-boot:run  (from ddd/order-multi/order-start/)
 *      OR: mvn install && java -jar order-start/target/order-start-1.0.0-SNAPSHOT.jar
 * API: http://localhost:8082/api/orders
 * H2 Console: http://localhost:8082/h2-console
 */
@SpringBootApplication
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
