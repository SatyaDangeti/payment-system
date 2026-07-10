package com.example.api_gateway.config;

import java.time.Instant;
import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = UUID.randomUUID().toString();

        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header("X-Trace-Id", traceId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        Instant start = Instant.now();

        System.out.println("======================================");
        System.out.println("🌐 API Gateway Request");
        System.out.println("🕒 Time      : " + start);
        System.out.println("🧾 Trace ID  : " + traceId);
        System.out.println("➡️ Method    : " + mutatedRequest.getMethod());
        System.out.println("➡️ Path      : " + mutatedRequest.getURI().getPath());
        System.out.println("➡️ Query     : " + mutatedRequest.getURI().getQuery());
        System.out.println("➡️ Client IP : " + exchange.getRequest().getRemoteAddress());
        System.out.println("======================================");

        return chain.filter(mutatedExchange)
                .then(Mono.fromRunnable(() -> {
                    System.out.println("======================================");
                    System.out.println("✅ API Gateway Response");
                    System.out.println("🧾 Trace ID  : " + traceId);
                    System.out.println("⬅️ Status    : " + exchange.getResponse().getStatusCode());
                    System.out.println("======================================");
                }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}