package com.preyansh.ridesharing.service;

import com.preyansh.ridesharing.dto.UserRequest;
import com.preyansh.ridesharing.dto.UserResponse;
import com.preyansh.ridesharing.exception.UserAlreadyExistsException;
import com.preyansh.ridesharing.model.User;
import com.preyansh.ridesharing.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> {
                    UserResponse userResponse = new UserResponse();
                    userResponse.setId(user.getId());
                    userResponse.setName(user.getName());
                    userResponse.setEmail(user.getEmail());
                    userResponse.setPhone(user.getPhone());

                    return userResponse;
                }).toList();
    }

    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    UserResponse userResponse = new UserResponse();
                    userResponse.setId(user.getId());
                    userResponse.setName(user.getName());
                    userResponse.setEmail(user.getEmail());
                    userResponse.setPhone(user.getPhone());

                    return userResponse;
                });
    }

    public UserResponse createUser(UserRequest userRequest) {
        Optional<User> existingUser = userRepository.findByEmail(userRequest.getEmail());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        User user = new User();
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());

        User savedUser = userRepository.save(user);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(savedUser.getId());
        userResponse.setName(savedUser.getName());
        userResponse.setEmail(savedUser.getEmail());
        userResponse.setPhone(savedUser.getPhone());

        return userResponse;
    }

    public Optional<UserResponse> updateUser(Long id, UserRequest updatedUserRequest) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(updatedUserRequest.getName());
                    user.setEmail(updatedUserRequest.getEmail());
                    user.setPhone(updatedUserRequest.getPhone());

                    User updatedUser = userRepository.save(user);

                    UserResponse userResponse = new UserResponse();
                    userResponse.setId(updatedUser.getId());
                    userResponse.setName(updatedUser.getName());
                    userResponse.setEmail(updatedUser.getEmail());
                    userResponse.setPhone(updatedUser.getPhone());

                    return userResponse;
                });
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
