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
import java.util.Optional;

// Marks this class as a Spring service component containing business logic.
@Service
public class UserService {

    // Repository used to perform database operations on User entities.
    private final UserRepository userRepository;

    // Constructor injection allows Spring to provide the UserRepository dependency.
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Retrieves users using pagination and converts each User entity into a UserResponse DTO.
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> {
                    UserResponse userResponse = new UserResponse();

                    // Copies the required entity fields into the response DTO.
                    userResponse.setId(user.getId());
                    userResponse.setName(user.getName());
                    userResponse.setEmail(user.getEmail());
                    userResponse.setPhone(user.getPhone());

                    return userResponse;
                });
    }

    // Retrieves a user by ID and returns the corresponding response DTO.
    public UserResponse getUserById(Long id) {

        // findById() returns an Optional because the user may not exist.
        Optional<User> existingUser = userRepository.findById(id);

        // Throws a custom exception if no user exists with the requested ID.
        if (existingUser.isEmpty()) {
            throw new UserNotFoundException("User with id " + id + " not found");
        }

        // Extracts the User entity after confirming that it exists.
        User user = existingUser.get();

        // Converts the entity into a response DTO.
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setName(user.getName());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhone(user.getPhone());

        return userResponse;
    }

    // Creates a new user after checking whether the email is already registered.
    public UserResponse createUser(UserRequest userRequest) {

        // Checks for an existing user with the same email address.
        Optional<User> existingUser = userRepository.findByEmail(userRequest.getEmail());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        // Creates a User entity and copies the incoming request data into it.
        User user = new User();
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());

        // Saves the new entity to the database.
        User savedUser = userRepository.save(user);

        // Converts the saved entity into a response DTO.
        UserResponse userResponse = new UserResponse();
        userResponse.setId(savedUser.getId());
        userResponse.setName(savedUser.getName());
        userResponse.setEmail(savedUser.getEmail());
        userResponse.setPhone(savedUser.getPhone());

        return userResponse;
    }

    // Updates all editable user fields after checking the user's existence
    // and ensuring the new email is not used by another user.
    public UserResponse updateUser(Long id, UserRequest updatedUserRequest) {

        // Retrieves the existing user by ID.
        Optional<User> existingUser = userRepository.findById(id);

        // Stops the update if the user does not exist.
        if (existingUser.isEmpty()) {
            throw new UserNotFoundException("User with id " + id + " not found");
        }

        User user = existingUser.get();

        // Checks whether another user already has the requested email.
        // The current user's ID is excluded from this check.
        Optional<User> existingUserWithEmail = userRepository.findByEmailAndIdNot(updatedUserRequest.getEmail(), id);

        if (existingUserWithEmail.isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        // Replaces the user's editable fields with the request values.
        user.setName(updatedUserRequest.getName());
        user.setEmail(updatedUserRequest.getEmail());
        user.setPhone(updatedUserRequest.getPhone());

        // Persists the updated entity.
        User updatedUser = userRepository.save(user);

        // Converts the updated entity into a response DTO.
        UserResponse userResponse = new UserResponse();

        userResponse.setId(updatedUser.getId());
        userResponse.setName(updatedUser.getName());
        userResponse.setEmail(updatedUser.getEmail());
        userResponse.setPhone(updatedUser.getPhone());

        return userResponse;
    }

    // Deletes a user by ID after verifying that the user exists.
    public void deleteUser(Long id) {

        // Checks whether a user exists with the specified ID.
        Optional<User> existingUser = userRepository.findById(id);

        // Throws an exception instead of silently deleting a nonexistent user.
        if (existingUser.isEmpty()) {
            throw new UserNotFoundException("User with id " + id + " not found");
        }

        // Deletes the user from the database.
        userRepository.deleteById(id);
    }

    // Partially updates a user's fields.
    // Only fields that are not null in the request are updated.
    public UserResponse patchUser(Long id, UserPatchRequest request) {

        // Retrieves the existing user by ID.
        Optional<User> existingUser = userRepository.findById(id);

        // Stops the update if the user does not exist.
        if (existingUser.isEmpty()) {
            throw new UserNotFoundException("User with id " + id + " not found");
        }

        User user = existingUser.get();

        // Updates the name only when a name was supplied.
        if (request.getName() != null) {
            user.setName(request.getName());
        }

        // Updates the email only when an email was supplied.
        if (request.getEmail() != null) {

            // Ensures another user does not already use the requested email.
            Optional<User> existingUserWithEmail = userRepository.findByEmailAndIdNot(request.getEmail(), id);

            if (existingUserWithEmail.isPresent()) {
                throw new UserAlreadyExistsException("User with this email already exists");
            }
            user.setEmail(request.getEmail());
        }

        // Updates the phone number only when a phone number was supplied.
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        // Saves the entity after applying the requested partial changes.
        User updatedUser = userRepository.save(user);

        // Converts the updated entity into a response DTO.
        UserResponse userResponse = new UserResponse();

        userResponse.setId(updatedUser.getId());
        userResponse.setName(updatedUser.getName());
        userResponse.setEmail(updatedUser.getEmail());
        userResponse.setPhone(updatedUser.getPhone());

        return userResponse;
    }

    // Searches for users whose names contain the supplied text, ignoring case.
    public List<UserResponse> searchUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name)
                .stream()
                // Converts each matching User entity into a UserResponse DTO.
                .map(user -> {
                    UserResponse userResponse = new UserResponse();

                    userResponse.setId(user.getId());
                    userResponse.setName(user.getName());
                    userResponse.setEmail(user.getEmail());
                    userResponse.setPhone(user.getPhone());

                    return userResponse;
                }).toList();
    }

    // Searches for users whose email addresses contain the supplied text, ignoring case.
    public List<UserResponse> searchUsersByEmail(String email) {
        return userRepository.findByEmailContainingIgnoreCase(email)
                .stream()
                // Converts each matching User entity into a UserResponse DTO.
                .map(user -> {
                    UserResponse userResponse = new UserResponse();

                    userResponse.setId(user.getId());
                    userResponse.setName(user.getName());
                    userResponse.setEmail(user.getEmail());
                    userResponse.setPhone(user.getPhone());

                    return userResponse;
                }).toList();
    }
}