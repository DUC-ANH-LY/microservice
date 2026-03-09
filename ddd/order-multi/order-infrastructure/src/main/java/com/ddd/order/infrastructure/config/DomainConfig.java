package com.ddd.order.infrastructure.config;

import com.ddd.order.domain.aggregate.order.OrderDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that registers domain services as beans.
 * Domain services are plain Java with no Spring annotations to keep domain framework-free.
 * This configuration lives in infrastructure where Spring is allowed.
 */
@Configuration
public class DomainConfig {

    @Bean
    public OrderDomainService orderDomainService() {
        return new OrderDomainService();
    }
}
