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

// Contains the business logic for ride requests.
@Service
public class RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final RideOfferService rideOfferService;

    // Constructor injection supplies the repositories required by this service.
    public RideService(
            RideRepository rideRepository,
            UserRepository userRepository,
            DriverRepository driverRepository,
            RideOfferService rideOfferService
    ) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.rideOfferService = rideOfferService;
    }

    // Retrieves all rides and returns them as response DTOs.
    public List<RideResponse> getRides() {
        return rideRepository.findAll()
                .stream()
                .map(ride -> new RideResponse(
                        ride.getId(),
                        ride.getRider().getId(),
                        ride.getDriver() == null ? null : ride.getDriver().getId(),
                        ride.getPickupLocation(),
                        ride.getDropoffLocation(),
                        ride.getStatus(),
                        ride.getRequestedAt()
                ))
                .toList();
    }

    // Retrieves a ride by its ID and returns its response DTO.
    public RideResponse getRideById(Long id) {
        Optional<Ride> existingRide = rideRepository.findById(id);

        if (existingRide.isEmpty()) {
            throw new RideNotFoundException("Ride with id " + id + " not found");
        }

        Ride ride = existingRide.get();

        return new RideResponse(
                ride.getId(),
                ride.getRider().getId(),
                ride.getDriver() == null ? null : ride.getDriver().getId(),
                ride.getPickupLocation(),
                ride.getDropoffLocation(),
                ride.getStatus(),
                ride.getRequestedAt()
        );
    }

    // Retrieves all rides belonging to a specific rider.
    public List<RideResponse> getRidesByRiderId(Long riderId) {
        return rideRepository.findByRiderId(riderId)
                .stream()
                .map(ride -> new RideResponse(
                        ride.getId(),
                        ride.getRider().getId(),
                        ride.getDriver() == null ? null : ride.getDriver().getId(),
                        ride.getPickupLocation(),
                        ride.getDropoffLocation(),
                        ride.getStatus(),
                        ride.getRequestedAt()
                ))
                .toList();
    }

    // Retrieves all rides assigned to a specific driver.
    public List<RideResponse> getRidesByDriverId(Long driverId) {
        return rideRepository.findByDriverId(driverId)
                .stream()
                .map(ride -> new RideResponse(
                        ride.getId(),
                        ride.getRider().getId(),
                        ride.getDriver().getId(),
                        ride.getPickupLocation(),
                        ride.getDropoffLocation(),
                        ride.getStatus(),
                        ride.getRequestedAt()
                ))
                .toList();
    }

    // Retrieves rides matching the specified status.
    public List<RideResponse> getRidesByStatus(RideStatus status) {
        return rideRepository.findByStatus(status)
                .stream()
                .map(ride -> new RideResponse(
                        ride.getId(),
                        ride.getRider().getId(),
                        ride.getDriver() == null ? null : ride.getDriver().getId(),
                        ride.getPickupLocation(),
                        ride.getDropoffLocation(),
                        ride.getStatus(),
                        ride.getRequestedAt()
                ))
                .toList();
    }

    // Creates a ride.
    @Transactional
    public RideResponse createRide(RideRequest request) {
        User rider = userRepository.findById(request.riderId())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + request.riderId() + " not found"
                        ));

        // Build a new ride using the validated request details.
        Ride ride = new Ride();
        ride.setRider(rider);
        ride.setPickupLocation(request.pickupLocation());
        ride.setDropoffLocation(request.dropoffLocation());
        ride.setStatus(RideStatus.REQUESTED);

        // Persist the ride. requestedAt is populated by the entity's @PrePersist callback.
        Ride savedRide = rideRepository.save(ride);

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

        // Lock the ride to prevent concurrent lifecycle changes.
        Ride ride = rideRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new RideNotFoundException(
                                "Ride with id " + id + " not found"
                        ));

        // Allow cancellation only for REQUESTED or ACCEPTED rides.
        if (ride.getStatus() != RideStatus.REQUESTED
                && ride.getStatus() != RideStatus.ACCEPTED) {
            throw new InvalidRideStatusException(
                    "Ride cannot be cancelled in its current state"
            );
        }

        // If the ride was accepted, lock its assigned driver before releasing them.
        if (ride.getStatus() == RideStatus.ACCEPTED
                && ride.getDriver() != null) {

            Driver driver = driverRepository.findByIdForUpdate(
                    ride.getDriver().getId()
            ).orElseThrow(() ->
                    new InvalidRideStatusException(
                            "Assigned driver could not be found"
                    ));

            driver.setStatus(DriverStatus.AVAILABLE);
            driverRepository.save(driver);
        }

        // Mark the ride as cancelled.
        ride.setStatus(RideStatus.CANCELLED);

        // Expire all offers that are still pending for this ride.
        rideOfferService.expirePendingOffers(id);

        // Save the cancelled ride.
        Ride savedRide = rideRepository.save(ride);

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

    // Starts a ride that has already been accepted by a driver.
    @Transactional
    public RideResponse startRide(Long rideId) {

        // Lock the ride to prevent concurrent lifecycle changes.
        Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() ->
                        new RideNotFoundException(
                                "Ride with id " + rideId + " not found"
                        ));

        // Only an ACCEPTED ride can be started.
        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new InvalidRideStatusException(
                    "Ride cannot be started in its current state"
            );
        }

        // Update the ride status.
        ride.setStatus(RideStatus.IN_PROGRESS);

        // JPA dirty checking persists this change when the transaction commits.
        return new RideResponse(
                ride.getId(),
                ride.getRider().getId(),
                ride.getDriver() == null ? null : ride.getDriver().getId(),
                ride.getPickupLocation(),
                ride.getDropoffLocation(),
                ride.getStatus(),
                ride.getRequestedAt()
        );
    }

    // Completes an in-progress ride.
    @Transactional
    public RideResponse completeRide(Long rideId) {

        // Lock the ride first to prevent concurrent lifecycle changes.
        Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() ->
                        new RideNotFoundException(
                                "Ride with id " + rideId + " not found"
                        ));

        // Only an IN_PROGRESS ride can be completed.
        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new InvalidRideStatusException(
                    "Ride cannot be completed in its current state"
            );
        }

        // Ensure the ride has an assigned driver before completing it.
        if (ride.getDriver() == null) {
            throw new InvalidRideStatusException(
                    "Ride cannot be completed because no driver is assigned"
            );
        }

        // Lock the assigned driver after locking the ride.
        Driver driver = driverRepository.findByIdForUpdate(
                ride.getDriver().getId()
        ).orElseThrow(() ->
                new InvalidRideStatusException(
                        "Assigned driver could not be found"
                ));

        // Mark the ride as completed.
        ride.setStatus(RideStatus.COMPLETED);

        // Make the assigned driver available for another ride.
        driver.setStatus(DriverStatus.AVAILABLE);
        driverRepository.save(driver);

        // Save the updated ride.
        Ride savedRide = rideRepository.save(ride);

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
