package com.preyansh.ridesharing.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.preyansh.ridesharing.dto.RegisterRequest;
import com.preyansh.ridesharing.dto.UserResponse;
import com.preyansh.ridesharing.exception.UserAlreadyExistsException;
import com.preyansh.ridesharing.model.Role;
import com.preyansh.ridesharing.model.User;
import com.preyansh.ridesharing.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection for repository and password encoder dependencies.
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Registers a new user and stores the BCrypt-hashed password.
    public UserResponse registerUser(RegisterRequest registerRequest) {

        // Normalize the email to avoid duplicate accounts with different casing.
        String email = registerRequest.getEmail().trim().toLowerCase(Locale.ROOT);

        // Check whether the email is already registered.
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        // Check whether the phone number is already registered.
        if (userRepository.existsByPhone(registerRequest.getPhone())) {
            throw new UserAlreadyExistsException("User with this phone already exists");
        }

        // Create a new user entity.
        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(email);
        user.setPhone(registerRequest.getPhone());

        // Public registration always assigns the RIDER role.
        user.setRole(Role.RIDER);

        // Hash the password before storing it in the database.
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));

        // Save the registered user.
        User savedUser = userRepository.save(user);

        // Create a response DTO without exposing the password hash.
        UserResponse response = new UserResponse();
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        response.setPhone(savedUser.getPhone());
        response.setRole(savedUser.getRole());

        return response;
    }
}