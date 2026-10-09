package com.preyansh.ridesharing.repository;

import java.util.List;

import com.preyansh.ridesharing.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Repository interface for performing database operations on User entities.
// Spring Data JPA generates the implementation automatically at runtime.
public interface UserRepository extends JpaRepository<User, Long> {

    // Finds users whose names contain the given text, ignoring case.
    List<User> findByNameContainingIgnoreCase(String name);

    // Finds users whose email addresses contain the given text, ignoring case.
    List<User> findByEmailContainingIgnoreCase(String email);

    // Finds a user with an exact email match.
    // Optional represents that a matching user may or may not exist.
    Optional<User> findByEmail(String email);

    // Finds a user with the specified email, excluding the user with the given ID.
    // Useful when checking email uniqueness during a user update.
    Optional<User> findByEmailAndIdNot(String email, Long id);
}