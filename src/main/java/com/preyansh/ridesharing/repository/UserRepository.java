package com.preyansh.ridesharing.repository;

import com.preyansh.ridesharing.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
