package com.preyansh.ridesharing.service;

import com.preyansh.ridesharing.dto.RideResponse;
import com.preyansh.ridesharing.exception.InvalidRideStatusException;
import com.preyansh.ridesharing.exception.RideNotFoundException;
import com.preyansh.ridesharing.model.Driver;
import com.preyansh.ridesharing.model.DriverStatus;
import com.preyansh.ridesharing.model.Ride;
import com.preyansh.ridesharing.model.RideStatus;
import com.preyansh.ridesharing.model.User;
import com.preyansh.ridesharing.repository.DriverRepository;
import com.preyansh.ridesharing.repository.RideRepository;
import com.preyansh.ridesharing.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Runs these tests with Mockito without starting Spring or connecting to PostgreSQL.
@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private RideOfferService rideOfferService;

    @InjectMocks
    private RideService rideService;

    private Ride ride;
    private User rider;

    @BeforeEach
    void setUp() {
        // Create an in-memory rider and ride for each independent test.
        // Create an in-memory rider without assigning a database-generated ID.
        rider = new User();
        // Create an in-memory ride without assigning a database-generated ID.
        ride = new Ride();
        ride.setRider(rider);
        ride.setPickupLocation("Baner, Pune");
        ride.setDropoffLocation("Pune Airport");
        ride.setStatus(RideStatus.REQUESTED);
    }

    @Test
    void cancelRequestedRideShouldCancelRideAndExpirePendingOffers() {
        // Arrange: the ride exists and has not yet been assigned to a driver.
        when(rideRepository.findByIdForUpdate(8L))
                .thenReturn(Optional.of(ride));

        // Return the same entity when the service saves it.
        when(rideRepository.save(ride)).thenReturn(ride);

        // Act: cancel the ride.
        RideResponse response = rideService.cancelRide(8L);

        // Assert: the ride is cancelled and its pending offers are expired.
        assertEquals(RideStatus.CANCELLED, response.status());
        verify(rideOfferService).expirePendingOffers(8L);
        verify(rideRepository).save(ride);

        // A requested ride has no assigned driver to release.
        verifyNoInteractions(driverRepository);
    }

    @Test
    void cancelAcceptedRideShouldReleaseDriverAndExpirePendingOffers() {
        // Arrange: create an accepted ride with an assigned, busy driver.
        Driver driver = new Driver();
        driver.setId(4L);
        driver.setStatus(DriverStatus.BUSY);

        ride.setDriver(driver);
        ride.setStatus(RideStatus.ACCEPTED);

        when(rideRepository.findByIdForUpdate(8L))
                .thenReturn(Optional.of(ride));
        when(rideRepository.save(ride)).thenReturn(ride);

        // Act: cancel the accepted ride.
        RideResponse response = rideService.cancelRide(8L);

        // Assert: the ride is cancelled and the driver becomes available.
        assertEquals(RideStatus.CANCELLED, response.status());
        assertEquals(DriverStatus.AVAILABLE, driver.getStatus());

        verify(driverRepository).save(driver);
        verify(rideOfferService).expirePendingOffers(8L);
        verify(rideRepository).save(ride);
    }

    @Test
    void cancelRideInInvalidStateShouldThrowException() {
        // Arrange: completed rides cannot be cancelled.
        ride.setStatus(RideStatus.COMPLETED);

        when(rideRepository.findByIdForUpdate(8L))
                .thenReturn(Optional.of(ride));

        // Act + Assert: the service rejects the invalid transition.
        assertThrows(
                InvalidRideStatusException.class,
                () -> rideService.cancelRide(8L)
        );

        // No updates should happen after the status validation fails.
        verify(rideRepository, never()).save(any(Ride.class));
        verify(rideOfferService, never()).expirePendingOffers(anyLong());
        verifyNoInteractions(driverRepository);
    }

    @Test
    void cancelNonexistentRideShouldThrowNotFoundException() {
        // Arrange: no ride exists with the requested ID.
        when(rideRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.empty());

        // Act + Assert: the service reports that the ride was not found.
        assertThrows(
                RideNotFoundException.class,
                () -> rideService.cancelRide(999L)
        );

        // No cancellation side effects should occur.
        verify(rideRepository, never()).save(any(Ride.class));
        verifyNoInteractions(driverRepository, rideOfferService);
    }

    @Test
    void startAcceptedRideShouldMoveToInProgress() {
        // Arrange: prepare a ride that has already been accepted.
        ride.setStatus(RideStatus.ACCEPTED);

        // Simulate the repository finding the ride.
        when(rideRepository.findById(8L))
                .thenReturn(Optional.of(ride));

        // Simulate saving the updated ride.
        when(rideRepository.save(ride)).thenReturn(ride);

        // Act: start the ride.
        RideResponse response = rideService.startRide(8L);

        // Assert: the ride should now be in progress.
        assertEquals(RideStatus.IN_PROGRESS, response.status());

        // Verify that the updated ride was saved.
        verify(rideRepository).save(ride);
    }

    @Test
    void startRideInInvalidStateShouldThrowException() {
        // Arrange: a requested ride cannot be started yet.
        ride.setStatus(RideStatus.REQUESTED);

        when(rideRepository.findById(8L))
                .thenReturn(Optional.of(ride));

        // Act + Assert: starting the ride should throw an exception.
        assertThrows(
                InvalidRideStatusException.class,
                () -> rideService.startRide(8L)
        );

        // Verify that no ride update was saved.
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void completeInProgressRideShouldCompleteAndReleaseDriver() {
        // Arrange: create a ride that is currently in progress.
        ride.setStatus(RideStatus.IN_PROGRESS);

        // Create a busy driver assigned to this ride.
        Driver driver = new Driver();
        driver.setStatus(DriverStatus.BUSY);
        ride.setDriver(driver);

        when(rideRepository.findById(8L))
                .thenReturn(Optional.of(ride));

        when(rideRepository.save(ride)).thenReturn(ride);

        // Act: complete the ride.
        RideResponse response = rideService.completeRide(8L);

        // Assert: the ride is completed and the driver is available.
        assertEquals(RideStatus.COMPLETED, response.status());
        assertEquals(DriverStatus.AVAILABLE, driver.getStatus());

        // Verify that both updated entities were saved.
        verify(rideRepository).save(ride);
        verify(driverRepository).save(driver);
    }

    @Test
    void completeRideInInvalidStateShouldThrowException() {
        // Arrange: the ride has not started yet.
        ride.setStatus(RideStatus.ACCEPTED);

        when(rideRepository.findById(8L))
                .thenReturn(Optional.of(ride));

        // Act + Assert: completion should be rejected.
        assertThrows(
                InvalidRideStatusException.class,
                () -> rideService.completeRide(8L)
        );

        // Verify that no updates were saved.
        verify(rideRepository, never()).save(any(Ride.class));
        verifyNoInteractions(driverRepository);
    }

    @Test
    void completeNonexistentRideShouldThrowNotFoundException() {
        // Arrange: simulate a ride that doesn't exist.
        when(rideRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act + Assert: the service should report the missing ride.
        assertThrows(
                RideNotFoundException.class,
                () -> rideService.completeRide(999L)
        );

        // Verify that no database updates were attempted.
        verify(rideRepository, never()).save(any(Ride.class));
        verifyNoInteractions(driverRepository);
    }
}