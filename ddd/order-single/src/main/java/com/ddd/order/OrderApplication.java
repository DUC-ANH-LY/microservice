package com.ddd.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DDD Order Demo — Single Module.
 *
 * All DDD layers co-exist as packages in a single Maven module.
 * Reference structure: https://github.com/Sayi/ddd-cargo (ddd-cargo-example)
 *
 * Run: mvn spring-boot:run  (from ddd/order-single/)
 * API: http://localhost:8081/api/orders
 * H2 Console: http://localhost:8081/h2-console
 */
@SpringBootApplication
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
