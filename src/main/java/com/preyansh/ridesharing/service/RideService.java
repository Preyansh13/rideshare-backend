package com.preyansh.ridesharing.service;

import com.preyansh.ridesharing.dto.RideRequest;
import com.preyansh.ridesharing.dto.RideResponse;
import com.preyansh.ridesharing.exception.UserNotFoundException;
import com.preyansh.ridesharing.model.Ride;
import com.preyansh.ridesharing.model.RideStatus;
import com.preyansh.ridesharing.model.User;
import com.preyansh.ridesharing.repository.RideRepository;
import com.preyansh.ridesharing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Contains the business logic for creating rides.
@Service
public class RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;

    // Constructor injection supplies the repositories required by this service.
    public RideService(RideRepository rideRepository, UserRepository userRepository) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RideResponse createRide(RideRequest request) {
        User rider = userRepository.findById(request.riderId())
                .orElseThrow(() -> new UserNotFoundException("User with id " + request.riderId() + " not found"));

        // Build a new ride using the validated request details.
        Ride ride = new Ride();
        ride.setRider(rider);
        ride.setPickupLocation(request.pickupLocation());
        ride.setDropoffLocation(request.dropoffLocation());
        ride.setStatus(RideStatus.REQUESTED);

        // Persist the ride. JPA generates its ID and requestedAt is
        // populated by the Ride entity's @PrePersist callback.
        Ride savedRide = rideRepository.save(ride);

        // Return a DTO rather than exposing the JPA entity directly.
        return new RideResponse(
                savedRide.getId(),
                savedRide.getRider().getId(),
                savedRide.getDriver() == null ? null : savedRide.getDriver().getId(),
                savedRide.getPickupLocation(),
                savedRide.getDropoffLocation(),
                savedRide.getStatus(),
                savedRide.getRequestedAt()
        );
    }
}
