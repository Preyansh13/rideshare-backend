package com.preyansh.ridesharing.service;

import com.preyansh.ridesharing.dto.UserPatchRequest;
import com.preyansh.ridesharing.dto.UserRequest;
import com.preyansh.ridesharing.dto.UserResponse;
import com.preyansh.ridesharing.exception.UserAlreadyExistsException;
import com.preyansh.ridesharing.exception.UserNotFoundException;
import com.preyansh.ridesharing.model.User;
import com.preyansh.ridesharing.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

// Contains business logic for user operations.
@Service
public class UserService {

    private final UserRepository userRepository;

    // Injects the UserRepository dependency.
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Retrieves a page of users and converts each entity into a response DTO.
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // Retrieves a user by ID or throws an exception if not found.
    public UserResponse getUserById(Long id) {
        return toResponse(findUserOrThrow(id));
    }

    // Creates a user after normalizing and checking the email.
    public UserResponse createUser(UserRequest userRequest) {
        // Normalizes the email before checking uniqueness and saving.
        String email = normalizeEmail(userRequest.getEmail());
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException(
                    "User with this email already exists");
        }
        if (userRepository.existsByPhone(userRequest.getPhone())) {
            throw new UserAlreadyExistsException(
                    "User with this phone number already exists");
        }
        // Creates a User entity from the request data.
        User user = new User();
        user.setName(userRequest.getName());
        user.setEmail(email);
        user.setPhone(userRequest.getPhone());
        // Saves the new user and returns its response DTO.
        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    // Updates all editable fields after checking the user's existence
    // and ensuring the normalized email is not used by another user.
    public UserResponse updateUser(Long id, UserRequest updatedUserRequest) {
        User user = findUserOrThrow(id);
        String email = normalizeEmail(updatedUserRequest.getEmail());
        // Excludes the current user from the duplicate-email check.
        if (userRepository.findByEmailAndIdNot(email, id).isPresent()) {
            throw new UserAlreadyExistsException(
                    "User with this email already exists");
        }
        if (userRepository.existsByPhoneAndIdNot(
                updatedUserRequest.getPhone(), id)) {
            throw new UserAlreadyExistsException(
                    "User with this phone number already exists");
        }
        // Updates the user's editable fields.
        user.setName(updatedUserRequest.getName());
        user.setEmail(email);
        user.setPhone(updatedUserRequest.getPhone());
        // Saves the updated entity and returns its response DTO.
        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    // Deletes a user after verifying that the user exists.
    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);
        userRepository.delete(user);
    }

    // Partially updates a user's fields when values are supplied.
    public UserResponse patchUser(Long id, UserPatchRequest request) {
        User user = findUserOrThrow(id);
        // Updates the name only if provided.
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        // Normalizes and checks the email only if provided.
        if (request.getEmail() != null) {
            String email = normalizeEmail(request.getEmail());
            if (userRepository.findByEmailAndIdNot(email, id).isPresent()) {
                throw new UserAlreadyExistsException(
                        "User with this email already exists");
            }
            user.setEmail(email);
        }
        // Updates the phone only if provided.
        if (request.getPhone() != null) {
            if (userRepository.existsByPhoneAndIdNot(request.getPhone(), id)) {
                throw new UserAlreadyExistsException(
                        "User with this phone number already exists");
            }
            user.setPhone(request.getPhone());
        }
        // Saves the partial changes and returns the response DTO.
        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    // Searches for users by name, ignoring case.
    public List<UserResponse> searchUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Searches for users by email, ignoring case.
    public List<UserResponse> searchUsersByEmail(String email) {
        return userRepository.findByEmailContainingIgnoreCase(email)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Removes surrounding whitespace and converts an email to lowercase.
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    // Converts a User entity into a response DTO.
    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        return response;
    }

    // Finds a user or throws an exception if the user does not exist.
    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " not found"));
    }
}
