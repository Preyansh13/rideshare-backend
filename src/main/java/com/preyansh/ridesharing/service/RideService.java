package com.preyansh.ridesharing.service;

import com.preyansh.ridesharing.dto.RideRequest;
import com.preyansh.ridesharing.dto.RideResponse;
import com.preyansh.ridesharing.exception.*;
import com.preyansh.ridesharing.model.*;
import com.preyansh.ridesharing.repository.DriverRepository;
import com.preyansh.ridesharing.repository.RideRepository;
import com.preyansh.ridesharing.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// Contains the business logic for rides requests.
@Service
public class RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final RideOfferService rideOfferService;

    // Constructor injection supplies the repositories required by this service.
    public RideService(RideRepository rideRepository, UserRepository userRepository, DriverRepository driverRepository, RideOfferService rideOfferService) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.rideOfferService = rideOfferService;
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

    // Retrieves all rides assigned to a specific driver.
    public List<RideResponse> getRidesByDriverId(Long driverId) {
        // Fetch rides associated with the specified driver from the database.
        return rideRepository.findByDriverId(driverId)
                .stream()
                // Convert each Ride entity into a RideResponse DTO.
                .map(ride -> new RideResponse(
                        ride.getId(),
                        ride.getRider().getId(),
                        // Retrieve the ID of the driver assigned to the ride.
                        ride.getDriver().getId(),
                        ride.getPickupLocation(),
                        ride.getDropoffLocation(),
                        ride.getStatus(),
                        ride.getRequestedAt()
                ))
                // Collect the converted responses into a list.
                .toList();
    }

    // Retrieves rides matching the specified status.
    public List<RideResponse> getRidesByStatus(RideStatus status) {
        // Fetch rides with the requested status from the database.
        return rideRepository.findByStatus(status)
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

    // Creates a ride.
    // @Transactional ensures database changes are handled within one transaction.
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

    // Cancels a ride if its current status allows cancellation.
    @Transactional
    public RideResponse cancelRide(Long id) {
        // Find the ride by its ID.
        Ride ride = rideRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new RideNotFoundException(
                                "Ride with id " + id + " not found"));
        // Allow cancellation only for REQUESTED or ACCEPTED rides.
        if (ride.getStatus() != RideStatus.REQUESTED
                && ride.getStatus() != RideStatus.ACCEPTED) {
            throw new InvalidRideStatusException(
                    "Ride cannot be cancelled in its current state");
        }
        // If the ride was accepted, make its assigned driver available.
        if (ride.getStatus() == RideStatus.ACCEPTED
                && ride.getDriver() != null) {
            Driver driver = ride.getDriver();
            driver.setStatus(DriverStatus.AVAILABLE);
            driverRepository.save(driver);
        }
        // Mark the ride as cancelled.
        ride.setStatus(RideStatus.CANCELLED);
        // Expire all offers that are still pending for this ride.
        rideOfferService.expirePendingOffers(id);
        // Save the cancelled ride.
        Ride savedRide = rideRepository.save(ride);
        // Return the updated ride as a response DTO.
        return new RideResponse(
                savedRide.getId(),
                savedRide.getRider().getId(),
                savedRide.getDriver() == null
                        ? null
                        : savedRide.getDriver().getId(),
                savedRide.getPickupLocation(),
                savedRide.getDropoffLocation(),
                savedRide.getStatus(),
                savedRide.getRequestedAt()
        );
    }

    // Accepts a requested ride for a driver who is currently available.
    // @Transactional ensures the database changes are committed together or rolled back if an unchecked exception occurs.
    @Transactional
    public RideResponse acceptRide(Long rideId, Long driverId) {
        // Accept the driver's pending offer and update the ride, driver, and other pending offers through RideOfferService.
        rideOfferService.acceptOffer(rideId, driverId);
        // Return the updated ride using the existing response format.
        return getRideById(rideId);
    }

    // Rejects a requested ride for a driver.
    @Transactional
    public RideResponse rejectRide(Long rideId, Long driverId) {
        // Reject only this driver's pending offer.
        rideOfferService.rejectOffer(rideId, driverId);
        // The ride remains REQUESTED, so return its current state.
        return getRideById(rideId);
    }

    // Starts a ride that has already been accepted by a driver.
    @Transactional
    public RideResponse startRide(Long rideId) {
        // Find the ride using its ID.
        Optional<Ride> existingRide = rideRepository.findById(rideId);
        // Throw an exception if the requested ride does not exist.
        if (existingRide.isEmpty()) {
            throw new RideNotFoundException("Ride with id " + rideId + " not found");
        }
        // Retrieve the ride entity after confirming it exists.
        Ride ride = existingRide.get();
        // Only an ACCEPTED ride can be started.
        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new InvalidRideStatusException("Ride cannot be started in its current state");
        }
        // Update the ride status because the journey has started.
        ride.setStatus(RideStatus.IN_PROGRESS);
        // Save the updated ride in the database.
        Ride savedRide = rideRepository.save(ride);
        // Convert the saved ride entity into a response DTO.
        // Return null for driverId if no driver is assigned.
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

    //Completes the in progress ride.
    @Transactional
    public RideResponse completeRide(Long rideId) {
        // Find the ride by its ID.
        Optional<Ride> existingRide = rideRepository.findById(rideId);
        // Throw an exception if the ride does not exist.
        if (existingRide.isEmpty()) {
            throw new RideNotFoundException("Ride with id " + rideId + " not found");
        }
        Ride ride = existingRide.get();
        // Only an in-progress ride can be completed.
        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new InvalidRideStatusException("Ride cannot be completed in its current state");
        }
        // Ensure the ride has an assigned driver before completing it.
        if (ride.getDriver() == null) {
            throw new InvalidRideStatusException("Ride cannot be completed because no driver is assigned");
        }
        // Mark the ride as completed.
        ride.setStatus(RideStatus.COMPLETED);
        // Make the assigned driver available for another ride.
        Driver driver = ride.getDriver();
        driver.setStatus(DriverStatus.AVAILABLE);
        driverRepository.save(driver);
        // Save the updated ride.
        Ride savedRide = rideRepository.save(ride);
        // Convert the saved entity into a response DTO.
        return new RideResponse(
                savedRide.getId(),
                savedRide.getRider().getId(),
                savedRide.getDriver().getId(),
                savedRide.getPickupLocation(),
                savedRide.getDropoffLocation(),
                savedRide.getStatus(),
                savedRide.getRequestedAt()
        );
    }
}
