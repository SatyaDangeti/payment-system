package com.payment.auth_service.service.impl;

import com.payment.auth_service.dto.AuthResponse;
import com.payment.auth_service.dto.LoginRequest;
import com.payment.auth_service.dto.RegisterRequest;
import com.payment.auth_service.entity.Role;
import com.payment.auth_service.entity.User;
import com.payment.auth_service.exception.UserAlreadyExistsException;
import com.payment.auth_service.exception.UserNotFoundException;
import com.payment.auth_service.repository.UserRepository;
import com.payment.auth_service.service.AuthService;
import com.payment.auth_service.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

@Override
public AuthResponse register(RegisterRequest request) {

    System.out.println("STEP A");

    if (userRepository.existsByEmail(request.getEmail())) {
        throw new UserAlreadyExistsException("Email already exists");
    }

    System.out.println("STEP B");

    User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.USER)
            .build();

    System.out.println("STEP C");

    userRepository.save(user);

    System.out.println("STEP D");

    String token = jwtService.generateToken(user);

    System.out.println("STEP E");

    return AuthResponse.builder()
            .token(token)
            .type("Bearer")
            .expiresIn(86400000L)
            .build();
}
    @Override
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
               .orElseThrow(() -> new UserNotFoundException("User not found"));

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(86400000L)
                .build();
    }
}