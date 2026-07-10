package com.example.api_gateway.service;

public interface JwtService {

    String extractUsername(String token);

    boolean isTokenValid(String token);
}