package com.preyansh.ridesharing.service;

import com.preyansh.ridesharing.dto.RideOfferResponse;
import com.preyansh.ridesharing.exception.DriverNotFoundException;
import com.preyansh.ridesharing.exception.InvalidDriverStatusException;
import com.preyansh.ridesharing.exception.InvalidRideStatusException;
import com.preyansh.ridesharing.exception.RideNotFoundException;
import com.preyansh.ridesharing.model.*;
import com.preyansh.ridesharing.repository.DriverRepository;
import com.preyansh.ridesharing.repository.RideOfferRepository;
import com.preyansh.ridesharing.repository.RideRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RideOfferService {

    private final RideOfferRepository rideOfferRepository;
    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;

    // Constructor injection makes the required repositories explicit.
    public RideOfferService(
            RideOfferRepository rideOfferRepository,
            RideRepository rideRepository,
            DriverRepository driverRepository) {
        this.rideOfferRepository = rideOfferRepository;
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
    }

    // Create an offer for one specific driver.
    @Transactional
    public RideOfferResponse createOffer(Long rideId, Long driverId) {
        // Lock the ride so two concurrent requests cannot assign it
        // through conflicting operations.
        Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() ->
                        new RideNotFoundException("Ride with id " + rideId + " not found"));
        // Offers are allowed only while the ride is waiting for a driver.
        if (ride.getStatus() != RideStatus.REQUESTED || ride.getDriver() != null) {
            throw new InvalidRideStatusException(
                    "Offers can only be created for rides awaiting a driver");
        }
        // Lock and validate the selected driver.
        Driver driver = driverRepository.findByIdForUpdate(driverId)
                .orElseThrow(() ->
                        new DriverNotFoundException("Driver with id: " + driverId + " not found"));
        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new InvalidDriverStatusException(
                    "An offer can only be created for an available driver");
        }
        // Verify the user has Driver role or not
        if (driver.getUser().getRole() != Role.DRIVER) {
            throw new InvalidDriverStatusException(
                    "Only users with the DRIVER role can receive ride offers"
            );
        }
        // Do not create duplicate offers for the same ride-driver pair.
        if (rideOfferRepository.existsByRide_IdAndDriver_Id(rideId, driverId)) {
            throw new InvalidRideStatusException(
                    "An offer already exists for this ride and driver");
        }
        // Create a new pending offer.
        RideOffer offer = new RideOffer();
        offer.setRide(ride);
        offer.setDriver(driver);
        offer.setStatus(RideOfferStatus.PENDING);
        // createdAt is populated by the entity's @PrePersist callback.
        RideOffer savedOffer = rideOfferRepository.save(offer);
        return toResponse(savedOffer);
    }

    // Reject an offer without rejecting the entire ride.
    @Transactional
    public RideOfferResponse rejectOffer(Long rideId, Long driverId) {
        // Lock the ride to prevent conflicting lifecycle operations.
        Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() ->
                        new RideNotFoundException(
                                "Ride with id " + rideId + " not found"));
        // A rejection is valid only while the ride is awaiting a driver.
        if (ride.getStatus() != RideStatus.REQUESTED || ride.getDriver() != null) {
            throw new InvalidRideStatusException(
                    "This ride is no longer accepting driver responses");
        }
        // Find the offer made to this particular driver.
        RideOffer offer = rideOfferRepository
                .findByRide_IdAndDriver_Id(rideId, driverId)
                .orElseThrow(() ->
                        new InvalidRideStatusException(
                                "No offer exists for this ride and driver"));
        // Only pending offers can be rejected.
        if (offer.getStatus() != RideOfferStatus.PENDING) {
            throw new InvalidRideStatusException(
                    "Only pending offers can be rejected");
        }
        // Retrieve the driver and ensure the associated user has the DRIVER role.
        Driver driver = driverRepository.findByIdForUpdate(driverId)
                .orElseThrow(() ->
                        new DriverNotFoundException(
                                "Driver with id " + driverId + " not found"));
        if (driver.getUser().getRole() != Role.DRIVER) {
            throw new InvalidDriverStatusException(
                    "Only users with the DRIVER role can reject ride offers");
        }
        // Reject this offer only. The ride remains REQUESTED.
        offer.setStatus(RideOfferStatus.REJECTED);
        // Save and return the updated offer.
        return toResponse(rideOfferRepository.save(offer));
    }

    // Accept an offer and assign the driver to the ride.
    @Transactional
    public RideOfferResponse acceptOffer(Long rideId, Long driverId) {
        Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() ->
                        new RideNotFoundException("Ride with id " + rideId + " not found"));
        if (ride.getStatus() != RideStatus.REQUESTED || ride.getDriver() != null) {
            throw new InvalidRideStatusException(
                    "This ride is no longer available for acceptance");
        }
        // The driver must have received an offer for this ride.
        RideOffer offer = rideOfferRepository
                .findByRide_IdAndDriver_Id(rideId, driverId)
                .orElseThrow(() ->
                        new InvalidRideStatusException(
                                "No offer exists for this ride and driver"));
        if (offer.getStatus() != RideOfferStatus.PENDING) {
            throw new InvalidRideStatusException(
                    "Only pending offers can be accepted");
        }
        // Lock and verify the driver before assigning the ride.
        Driver driver = driverRepository.findByIdForUpdate(driverId)
                .orElseThrow(() ->
                        new DriverNotFoundException("Driver with id " + driverId + " not found"));
        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new InvalidDriverStatusException(
                    "Driver is no longer available");
        }
        // Verify the role of user is Driver or not
        if (driver.getUser().getRole() != Role.DRIVER) {
            throw new InvalidDriverStatusException(
                    "Only users with the DRIVER role can accept rides"
            );
        }
        // Accept the selected offer.
        offer.setStatus(RideOfferStatus.ACCEPTED);
        // Assign the driver and update the ride's lifecycle status.
        ride.setDriver(driver);
        ride.setStatus(RideStatus.ACCEPTED);
        // An assigned driver cannot accept another ride while busy.
        driver.setStatus(DriverStatus.BUSY);
        // Expire all other offers that are still pending for this ride.
        List<RideOffer> otherPendingOffers =
                rideOfferRepository.findByRide_IdAndStatus(
                        rideId, RideOfferStatus.PENDING);
        for (RideOffer otherOffer : otherPendingOffers) {
            if (!otherOffer.getId().equals(offer.getId())) {
                otherOffer.setStatus(RideOfferStatus.EXPIRED);
            }
        }
        // Persist the related changes within the same transaction.
        rideOfferRepository.saveAll(otherPendingOffers);
        rideOfferRepository.save(offer);
        rideRepository.save(ride);
        driverRepository.save(driver);
        return toResponse(offer);
    }

    // Convert the entity into a response DTO.
    private RideOfferResponse toResponse(RideOffer offer) {
        return new RideOfferResponse(
                offer.getId(),
                offer.getRide().getId(),
                offer.getDriver().getId(),
                offer.getStatus(),
                offer.getCreatedAt()
        );
    }
}
