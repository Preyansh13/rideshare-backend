package com.preyansh.ridesharing.repository;

import com.preyansh.ridesharing.model.RideOffer;
import com.preyansh.ridesharing.model.RideOfferStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Provides database operations for RideOffer entities.
// Spring Data JPA generates the implementation automatically.
public interface RideOfferRepository extends JpaRepository<RideOffer, Long> {
    // Finds an offer associated with a particular ride and driver.
    // Useful when a driver accepts or rejects their own offer.
    Optional<RideOffer> findByRide_IdAndDriver_Id(Long rideId, Long driverId);
    // Checks whether an offer already exists for this ride-driver pair.
    // Helps prevent duplicate offers before attempting to save one.
    boolean existsByRide_IdAndDriver_Id(Long rideId, Long driverId);
    // Retrieves all offers for a particular ride with the specified status.
    // For example, find all PENDING offers for Ride 10.
    List<RideOffer> findByRide_IdAndStatus(Long rideId, RideOfferStatus status);
    // Retrieves all offers assigned to a particular driver with the specified status.
    // For example, find all PENDING offers for Driver 1.
    List<RideOffer> findByDriver_IdAndStatus(Long driverId, RideOfferStatus status);
}
