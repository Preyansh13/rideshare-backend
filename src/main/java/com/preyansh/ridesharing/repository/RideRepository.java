package com.preyansh.ridesharing.repository;

import com.preyansh.ridesharing.model.Ride;
import com.preyansh.ridesharing.model.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Provides database operations for Ride entities through Spring Data JPA.
public interface RideRepository extends JpaRepository<Ride, Long> {
    // Retrieves all rides belonging to a specific rider.
    List<Ride> findByRiderId(Long riderId);

    // Retrieves all rides matching the specified status.
    List<Ride> findByStatus(RideStatus status);

    // Retrieves rides assigned to a specific driver.
    List<Ride> findByDriverId(Long driverId);
}
