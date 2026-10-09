package com.preyansh.ridesharing.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;

import com.preyansh.ridesharing.service.UserService;
import com.preyansh.ridesharing.dto.UserRequest;
import com.preyansh.ridesharing.dto.UserResponse;
import com.preyansh.ridesharing.dto.UserPatchRequest;

// Marks this class as a REST controller that handles HTTP requests
// and returns response data, typically in JSON format.
@RestController

// Defines the base URL for all endpoints in this controller.
@RequestMapping("/api/users")
public class UserController {

    // Final dependency ensures the service is provided through the constructor.
    private final UserService userService;

    // Constructor injection: Spring provides the UserService instance.
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users
    // Retrieves users in a paginated format using the supplied pagination parameters.
    @GetMapping
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userService.getUsers(pageable);
    }

    // GET /api/users/{id}
    // Retrieves a specific user by ID and returns HTTP 200 OK.
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse userResponse = userService.getUserById(id);
        return ResponseEntity.ok(userResponse);
    }

    // POST /api/users
    // Validates the request body, creates a user, and returns HTTP 201 Created.
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse savedUser = userService.createUser(userRequest);
        return ResponseEntity.status(201).body(savedUser);
    }

    // PUT /api/users/{id}
    // Replaces the user's editable details using the supplied request data.
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest updatedUserRequest) {
        UserResponse updatedUser = userService.updateUser(id, updatedUserRequest);
        return ResponseEntity.ok(updatedUser);
    }

    // DELETE /api/users/{id}
    // Deletes the specified user and returns HTTP 204 No Content on success.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/users/{id}
    // Partially updates a user using the fields provided in the request.
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> patchUser(@PathVariable Long id, @RequestBody UserPatchRequest request) {
        UserResponse updatedUser = userService.patchUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    // GET /api/users/search/name?name=...
    // Searches users by name. Rejects a missing or blank search parameter.
    @GetMapping("/search/name")
    public ResponseEntity<List<UserResponse>> searchUsersByName(@RequestParam(required = false) String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name search parameter cannot be empty");
        }
        return ResponseEntity.ok(userService.searchUsersByName(name));
    }

    // GET /api/users/search/email?email=...
    // Searches for users by email and returns the matching users.
    @GetMapping("/search/email")
    public List<UserResponse> searchUsersByEmail(@RequestParam String email) {
        return userService.searchUsersByEmail(email);
    }
}