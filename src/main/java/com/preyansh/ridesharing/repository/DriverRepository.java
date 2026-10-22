package com.preyansh.ridesharing.repository;

import com.preyansh.ridesharing.model.Driver;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// Repository interface for performing database operations on Driver entities.
public interface DriverRepository extends JpaRepository<Driver, Long> {
    // Retrieves a driver while acquiring a pessimistic write lock.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Driver d WHERE d.id = :id")
    Optional<Driver> findByIdForUpdate(@Param("id") Long id);
}
