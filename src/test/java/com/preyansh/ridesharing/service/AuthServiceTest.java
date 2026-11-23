package com.preyansh.ridesharing.service;

import com.preyansh.ridesharing.dto.RegisterRequest;
import com.preyansh.ridesharing.dto.UserResponse;
import com.preyansh.ridesharing.exception.UserAlreadyExistsException;
import com.preyansh.ridesharing.model.Role;
import com.preyansh.ridesharing.model.User;
import com.preyansh.ridesharing.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    // REGISTER USER
    // Verifies successful registration, including email normalization,
    // password hashing, and automatic RIDER role assignment.
    @Test
    void registerUser_shouldRegisterUserSuccessfully() {

        RegisterRequest request = createRegisterRequest(
                "Preyansh Sharma",
                " PREYANSH@GMAIL.COM ",
                "9876543210",
                "password123"
        );

        // Indicate that the email and phone are not already registered.
        when(userRepository.findByEmail("preyansh@gmail.com"))
                .thenReturn(Optional.empty());

        when(userRepository.existsByPhone("9876543210"))
                .thenReturn(false);

        // Mock BCrypt encoding.
        when(passwordEncoder.encode("password123"))
                .thenReturn("hashedPassword");

        // Return the saved user with a generated ID.
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User savedUser = invocation.getArgument(0);
                    savedUser.setId(1L);
                    return savedUser;
                });

        // Register the user.
        UserResponse result = authService.registerUser(request);

        // Verify that the response contains the correct user details.
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Preyansh Sharma", result.getName());
        assertEquals("preyansh@gmail.com", result.getEmail());
        assertEquals("9876543210", result.getPhone());
        assertEquals(Role.RIDER, result.getRole());

        // Verify that the password was encoded before saving.
        verify(passwordEncoder).encode("password123");

        // Verify that the saved entity contains the encoded password.
        verify(userRepository).save(argThat(user ->
                "hashedPassword".equals(user.getPasswordHash())
                        && user.getRole() == Role.RIDER
        ));

        // Verify that the plain-text password is not returned.
        verify(userRepository).findByEmail("preyansh@gmail.com");
        verify(userRepository).existsByPhone("9876543210");
    }

    // Verifies that registration is rejected when the email already exists.
    @Test
    void registerUser_shouldThrowExceptionWhenEmailAlreadyExists() {

        RegisterRequest request = createRegisterRequest(
                "Preyansh Sharma",
                "preyansh@gmail.com",
                "9876543210",
                "password123"
        );

        User existingUser = new User();

        // Indicate that the email is already registered.
        when(userRepository.findByEmail("preyansh@gmail.com"))
                .thenReturn(Optional.of(existingUser));

        // Verify that the duplicate-user exception is thrown.
        assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.registerUser(request)
        );

        // No password encoding or saving should happen.
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).existsByPhone(any());
    }

    // Verifies that registration is rejected when the phone number already exists.
    @Test
    void registerUser_shouldThrowExceptionWhenPhoneAlreadyExists() {

        RegisterRequest request = createRegisterRequest(
                "Preyansh Sharma",
                "preyansh@gmail.com",
                "9876543210",
                "password123"
        );

        // Indicate that the email is available.
        when(userRepository.findByEmail("preyansh@gmail.com"))
                .thenReturn(Optional.empty());

        // Indicate that the phone number is already registered.
        when(userRepository.existsByPhone("9876543210"))
                .thenReturn(true);

        // Verify that the duplicate-user exception is thrown.
        assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.registerUser(request)
        );

        // No password encoding or saving should happen.
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    // HELPER METHODS
    // Creates a RegisterRequest DTO with the supplied test data.
    private RegisterRequest createRegisterRequest(
            String name,
            String email,
            String phone,
            String password
    ) {
        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPhone(phone);
        request.setPassword(password);
        return request;
    }
}