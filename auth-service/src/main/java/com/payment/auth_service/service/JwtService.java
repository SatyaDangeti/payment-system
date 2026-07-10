package com.payment.auth_service.service;

import org.springframework.security.core.userdetails.UserDetails;

import com.payment.auth_service.entity.User;

public interface JwtService {

    String generateToken(User user);

    String extractUsername(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
    

}