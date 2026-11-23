package com.preyansh.ridesharing.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.preyansh.ridesharing.dto.RegisterRequest;
import com.preyansh.ridesharing.dto.UserResponse;
import com.preyansh.ridesharing.service.AuthService;

import jakarta.validation.Valid;

// REST controller that handles authentication-related requests.
@RestController
// Defines the base URL for authentication endpoints.
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // Constructor injection for the authentication service.
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Registers a new user and returns the created user's details.
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody RegisterRequest registerRequest) {

        // Delegate registration logic to the authentication service.
        UserResponse response = authService.registerUser(registerRequest);

        // Return HTTP 201 Created with the registered user's details.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}