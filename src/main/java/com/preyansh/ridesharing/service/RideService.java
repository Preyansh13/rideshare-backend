package com.preyansh.ridesharing.service;

import com.preyansh.ridesharing.dto.RideRequest;
import com.preyansh.ridesharing.dto.RideResponse;
import com.preyansh.ridesharing.exception.RideNotFoundException;
import com.preyansh.ridesharing.exception.UserNotFoundException;
import com.preyansh.ridesharing.model.Ride;
import com.preyansh.ridesharing.model.RideStatus;
import com.preyansh.ridesharing.model.User;
import com.preyansh.ridesharing.repository.RideRepository;
import com.preyansh.ridesharing.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    // Retrieves all rides and returns them as response DTOs.
    public List<RideResponse> getRides() {

        // Fetch all rides from the database, then convert each entity into a DTO.
        return rideRepository.findAll()
                .stream()
                .map(ride -> new RideResponse(
                        ride.getId(),
                        ride.getRider().getId(),

                        // Return null when the ride has not been assigned a driver.
                        ride.getDriver() == null ? null : ride.getDriver().getId(),

                        ride.getPickupLocation(),
                        ride.getDropoffLocation(),
                        ride.getStatus(),
                        ride.getRequestedAt()
                ))
                // Collect all converted DTOs into a list.
                .toList();
    }

    // Retrieves a ride by its ID and returns its response DTO.
    public RideResponse getRideById(Long id) {

        // Search for the ride using its ID.
        Optional<Ride> existingRide = rideRepository.findById(id);

        // Throw an exception if the requested ride does not exist.
        if (existingRide.isEmpty()) {
            throw new RideNotFoundException("Ride with id " + id + " not found");
        }

        // Retrieve the Ride entity after confirming it exists.
        Ride ride = existingRide.get();

        // Convert the Ride entity into a response DTO.
        // Return null for driverId when no driver has been assigned.
        RideResponse rideResponse = new RideResponse(
                ride.getId(),
                ride.getRider().getId(),
                ride.getDriver() == null ? null : ride.getDriver().getId(),
                ride.getPickupLocation(),
                ride.getDropoffLocation(),
                ride.getStatus(),
                ride.getRequestedAt()
        );

        // Return the ride details to the caller.
        return rideResponse;
    }

    // Retrieves all rides belonging to a specific rider.
    public List<RideResponse> getRidesByRiderId(Long riderId) {
        // Fetch rides associated with the given rider ID from the database.
        return rideRepository.findByRiderId(riderId)
                .stream()
                // Convert each Ride entity into a RideResponse DTO.
                .map(ride -> new RideResponse(
                        ride.getId(),
                        ride.getRider().getId(),

                        // Return null if no driver has been assigned to the ride.
                        ride.getDriver() == null ? null : ride.getDriver().getId(),

                        ride.getPickupLocation(),
                        ride.getDropoffLocation(),
                        ride.getStatus(),
                        ride.getRequestedAt()
                ))
                // Collect the converted DTOs into a list and return it.
                .toList();
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
