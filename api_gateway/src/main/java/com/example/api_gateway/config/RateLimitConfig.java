package com.example.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String hostAddress = "unknown";

            if (exchange.getRequest().getRemoteAddress() != null
                    && exchange.getRequest().getRemoteAddress().getAddress() != null) {
                hostAddress = exchange.getRequest()
                        .getRemoteAddress()
                        .getAddress()
                        .getHostAddress();
            }

            return Mono.just(hostAddress);
        };
    }
}