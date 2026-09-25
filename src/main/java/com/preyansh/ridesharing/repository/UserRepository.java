package com.preyansh.ridesharing.repository;

import java.util.List;

import com.preyansh.ridesharing.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByNameContainingIgnoreCase(String name);
    List<User> findByEmailContainingIgnoreCase(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndIdNot(String email, Long id);
}
