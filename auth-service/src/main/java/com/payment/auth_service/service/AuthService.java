package com.payment.auth_service.service;

import com.payment.auth_service.dto.AuthResponse;
import com.payment.auth_service.dto.LoginRequest;
import com.payment.auth_service.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

}