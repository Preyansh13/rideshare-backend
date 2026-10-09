package com.preyansh.ridesharing.repository;

import com.preyansh.ridesharing.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

// Repository interface for performing database operations on Driver entities.
// Spring Data JPA automatically provides the implementation at runtime.
public interface DriverRepository extends JpaRepository<Driver, Long> {
}