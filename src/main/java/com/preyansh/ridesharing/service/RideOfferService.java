package com.preyansh.ridesharing.service;

import com.preyansh.ridesharing.dto.RideOfferResponse;
import com.preyansh.ridesharing.exception.*;
import com.preyansh.ridesharing.model.*;
import com.preyansh.ridesharing.repository.DriverRepository;
import com.preyansh.ridesharing.repository.RideOfferRepository;
import com.preyansh.ridesharing.repository.RideRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RideOfferService {

    private final RideOfferRepository rideOfferRepository;
    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;

    public RideOfferService(
            RideOfferRepository rideOfferRepository,
            RideRepository rideRepository,
            DriverRepository driverRepository
    ) {
        this.rideOfferRepository = rideOfferRepository;
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
    }

    // Creates an offer for an available driver on a requested ride.
    @Transactional
    public RideOfferResponse createOffer(Long rideId, Long driverId) {

        // Lock the ride first to serialize offer creation for this ride.
        Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() ->
                        new RideNotFoundException(
                                "Ride with id " + rideId + " not found"
                        ));

        if (ride.getStatus() != RideStatus.REQUESTED
                || ride.getDriver() != null) {
            throw new InvalidRideStatusException(
                    "Offers can only be created for unassigned requested rides"
            );
        }

        // Lock the driver before checking availability.
        Driver driver = driverRepository.findByIdForUpdate(driverId)
                .orElseThrow(() ->
                        new DriverNotFoundException(
                                "Driver with id " + driverId + " not found"
                        ));

        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new InvalidDriverStatusException(
                    "Driver is not available"
            );
        }

        if (driver.getUser().getRole() != Role.DRIVER) {
            throw new InvalidDriverStatusException(
                    "Only users with the DRIVER role can receive ride offers"
            );
        }

        // Check for an existing offer before creating another one.
        if (rideOfferRepository.existsByRide_IdAndDriver_Id(
                rideId, driverId
        )) {
            throw new InvalidRideStatusException(
                    "An offer already exists for this ride and driver"
            );
        }

        RideOffer offer = new RideOffer();
        offer.setRide(ride);
        offer.setDriver(driver);
        offer.setStatus(RideOfferStatus.PENDING);

        RideOffer savedOffer = rideOfferRepository.save(offer);

        return toResponse(savedOffer);
    }

    // Rejects a pending offer without changing the driver's availability.
    @Transactional
    public RideOfferResponse rejectOffer(Long rideId, Long driverId) {

        // Lock the ride to serialize rejection against acceptance and cancellation.
        Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() ->
                        new RideNotFoundException(
                                "Ride with id " + rideId + " not found"
                        ));

        if (ride.getStatus() != RideStatus.REQUESTED
                || ride.getDriver() != null) {
            throw new InvalidRideStatusException(
                    "This ride is no longer accepting offer responses"
            );
        }

        RideOffer offer = rideOfferRepository
                .findByRide_IdAndDriver_Id(rideId, driverId)
                .orElseThrow(() ->
                        new InvalidRideStatusException(
                                "No offer exists for this ride and driver"
                        ));

        if (offer.getStatus() != RideOfferStatus.PENDING) {
            throw new InvalidRideStatusException(
                    "Only pending offers can be rejected"
            );
        }

        // The offer already references its driver, so another database lock
        // is unnecessary because rejection does not modify driver state.
        if (offer.getDriver().getUser().getRole() != Role.DRIVER) {
            throw new InvalidDriverStatusException(
                    "Only users with the DRIVER role can reject ride offers"
            );
        }

        offer.setStatus(RideOfferStatus.REJECTED);

        RideOffer savedOffer = rideOfferRepository.save(offer);

        return toResponse(savedOffer);
    }

    // Accepts a pending offer and assigns its driver to the ride.
    @Transactional
    public RideOfferResponse acceptOffer(Long rideId, Long driverId) {

        // Always lock the ride before the driver to maintain a consistent lock order.
        Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() ->
                        new RideNotFoundException(
                                "Ride with id " + rideId + " not found"
                        ));

        if (ride.getStatus() != RideStatus.REQUESTED
                || ride.getDriver() != null) {
            throw new InvalidRideStatusException(
                    "This ride is no longer available for acceptance"
            );
        }

        RideOffer offer = rideOfferRepository
                .findByRide_IdAndDriver_Id(rideId, driverId)
                .orElseThrow(() ->
                        new InvalidRideStatusException(
                                "No offer exists for this ride and driver"
                        ));

        if (offer.getStatus() != RideOfferStatus.PENDING) {
            throw new InvalidRideStatusException(
                    "Only pending offers can be accepted"
            );
        }

        // Lock the driver before checking and changing availability.
        Driver driver = driverRepository.findByIdForUpdate(driverId)
                .orElseThrow(() ->
                        new DriverNotFoundException(
                                "Driver with id " + driverId + " not found"
                        ));

        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new InvalidDriverStatusException(
                    "Driver is not available"
            );
        }

        if (driver.getUser().getRole() != Role.DRIVER) {
            throw new InvalidDriverStatusException(
                    "Only users with the DRIVER role can accept ride offers"
            );
        }

        // Accept the selected offer and assign its driver to the ride.
        offer.setStatus(RideOfferStatus.ACCEPTED);
        ride.setDriver(driver);
        ride.setStatus(RideStatus.ACCEPTED);
        driver.setStatus(DriverStatus.BUSY);

        // Expire all other pending offers for this ride.
        List<RideOffer> pendingOffers =
                rideOfferRepository.findByRide_IdAndStatus(
                        rideId, RideOfferStatus.PENDING
                );

        for (RideOffer pendingOffer : pendingOffers) {
            if (!pendingOffer.getId().equals(offer.getId())) {
                pendingOffer.setStatus(RideOfferStatus.EXPIRED);
            }
        }

        rideOfferRepository.saveAll(pendingOffers);
        rideOfferRepository.save(offer);
        rideRepository.save(ride);
        driverRepository.save(driver);

        return toResponse(offer);
    }

    // Expires all pending offers associated with a ride.
    @Transactional
    public void expirePendingOffers(Long rideId) {

        List<RideOffer> pendingOffers =
                rideOfferRepository.findByRide_IdAndStatus(
                        rideId, RideOfferStatus.PENDING
                );

        for (RideOffer offer : pendingOffers) {
            offer.setStatus(RideOfferStatus.EXPIRED);
        }

        if (!pendingOffers.isEmpty()) {
            rideOfferRepository.saveAll(pendingOffers);
        }
    }

    // Converts an entity into its response DTO.
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
