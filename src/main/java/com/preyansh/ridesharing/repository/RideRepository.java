package com.preyansh.ridesharing.repository;

import com.preyansh.ridesharing.model.Ride;
import com.preyansh.ridesharing.model.RideStatus;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// Provides database operations for Ride entities through Spring Data JPA.
public interface RideRepository extends JpaRepository<Ride, Long> {
    // Retrieves all rides belonging to a specific rider.
    List<Ride> findByRiderId(Long riderId);
    // Retrieves all rides matching the specified status.
    List<Ride> findByStatus(RideStatus status);
    // Retrieves rides assigned to a specific driver.
    List<Ride> findByDriverId(Long driverId);
    // Locks a ride row while it is being processed in a transaction.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Ride r WHERE r.id = :id")
    Optional<Ride> findByIdForUpdate(@Param("id") Long id);
}
