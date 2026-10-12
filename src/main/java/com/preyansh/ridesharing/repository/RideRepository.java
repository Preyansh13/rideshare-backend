package com.preyansh.ridesharing.repository;

import com.preyansh.ridesharing.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

// Provides database operations for Ride entities through Spring Data JPA.
public interface RideRepository extends JpaRepository<Ride, Long> {
}
