package com.preyansh.ridesharing.repository;

import com.preyansh.ridesharing.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

// Repository interface for performing database operations on Vehicle entities.
// Spring Data JPA automatically generates its implementation at runtime.
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}