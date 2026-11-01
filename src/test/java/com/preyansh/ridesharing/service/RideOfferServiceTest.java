package com.preyansh.ridesharing.service;

import com.preyansh.ridesharing.dto.*;
import com.preyansh.ridesharing.exception.*;
import com.preyansh.ridesharing.model.*;
import com.preyansh.ridesharing.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class RideOfferServiceTest {

    // Mock the repositories so tests don't connect to PostgreSQL.
    @Mock
    private RideOfferRepository rideOfferRepository;

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverRepository driverRepository;

    // Mockito injects the three mocked repositories into this service.
    @InjectMocks
    private RideOfferService rideOfferService;

    // Real objects used as test data.
    private Ride ride;
    private Driver driver;
    private User user;
    private RideOffer rideOffer;

    @BeforeEach
    void setUp() {

        // Create a user who has the DRIVER role.
        user = new User();
        user.setId(1L);
        user.setName("Test Driver");
        user.setEmail("driver@test.com");
        user.setPhone("9876543210");
        user.setRole(Role.DRIVER);

        // Create an available driver linked to the user.
        driver = new Driver();
        driver.setId(1L);
        driver.setUser(user);
        driver.setLicenseNumber("DL123456");
        driver.setStatus(DriverStatus.AVAILABLE);

        // Create a ride that is waiting for a driver.
        ride = new Ride();
        ride.setId(1L);
        ride.setPickupLocation("Pune");
        ride.setDropoffLocation("Mumbai");
        ride.setStatus(RideStatus.REQUESTED);

        // Create a pending offer linked to the ride and driver.
        rideOffer = new RideOffer();
        rideOffer.setRide(ride);
        rideOffer.setDriver(driver);
        rideOffer.setStatus(RideOfferStatus.PENDING);
    }

    @Test
    void createOffer_ShouldCreateOffer_WhenRideAndDriverAreValid() {

        // Arrange: Configure repositories to return valid test data
        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));
        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));
        when(rideOfferRepository.existsByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(false);

        // Make the repository return the offer that the service saves
        when(rideOfferRepository.save(any(RideOffer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Execute the service method.
        RideOfferResponse response = rideOfferService.createOffer(1L, 1L);

        // Assert: Verify the response and saved offer.
        assertNotNull(response);
        assertEquals(1L, response.rideId());
        assertEquals(1L, response.driverId());
        assertEquals(RideOfferStatus.PENDING, response.status());

        verify(rideOfferRepository).save(any(RideOffer.class));
    }

    @Test
    void createOffer_ShouldThrowException_WhenOfferAlreadyExists() {

        // Arrange: Return an existing ride.
        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        // Arrange: Return an available driver.
        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));

        // Simulate an offer already existing for this ride and driver.
        when(rideOfferRepository.existsByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(true);

        // Act & Assert: The service should reject the duplicate offer.
        assertThrows(
                InvalidRideStatusException.class,
                () -> rideOfferService.createOffer(1L, 1L)
        );

        // Verify that no new offer was saved.
        verify(rideOfferRepository, never()).save(any(RideOffer.class));
    }

    @Test
    void createOffer_ShouldThrowException_WhenDriverIsUnavailable() {

        // Arrange: Return an existing requested ride.
        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        // Make the driver unavailable.
        driver.setStatus(DriverStatus.BUSY);

        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));

        // Act & Assert: Creating an offer should fail.
        assertThrows(
                InvalidDriverStatusException.class,
                () -> rideOfferService.createOffer(1L, 1L)
        );

        // Verify that no offer was saved.
        verify(rideOfferRepository, never()).save(any(RideOffer.class));
    }

    @Test
    void rejectOffer_ShouldRejectOffer_WhenOfferIsPending() {

        // ARRANGE
        // Return the existing ride when the service searches for it.
        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));
        // Return the pending offer associated with this ride and driver.
        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));
        // Return the available driver associated with the offer.
        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));
        // Simulate saving the offer by returning the same object passed to save().
        when(rideOfferRepository.save(any(RideOffer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        // Execute the service method to reject the pending offer.
        RideOfferResponse response = rideOfferService.rejectOffer(1L, 1L);

        // ASSERT
        assertEquals(RideOfferStatus.REJECTED, response.status()); // Verify that the returned response has REJECTED status.
        assertEquals(RideStatus.REQUESTED, ride.getStatus()); // Verify that rejecting an offer does not change the ride's status.
        assertEquals(DriverStatus.AVAILABLE, driver.getStatus()); // Verify that the driver remains available after rejecting the offer.

        // Verify that the offer was saved in the repository.
        verify(rideOfferRepository).save(any(RideOffer.class));
    }

    @Test
    void rejectOffer_ShouldThrowException_WhenOfferIsAlreadyProcessed() {

        // Arrange
        // Return the ride when the service searches for it.
        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));
        // Simulate an offer that has already been rejected.
        // The service should only allow PENDING offers to be rejected.
        rideOffer.setStatus(RideOfferStatus.REJECTED);
        // Return the already-processed offer.
        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));

        // Act & Assert
        // Expect InvalidRideStatusException because the offer is not PENDING.
        assertThrows(
                InvalidRideStatusException.class,
                () -> rideOfferService.rejectOffer(1L, 1L)
        );

        // Verify that the offer was not saved because the operation failed.
        verify(rideOfferRepository, never()).save(any(RideOffer.class));
    }

    @Test
    void rejectOffer_ShouldThrowException_WhenOfferDoesNotExist() {

        // Arrange
        // Return the ride when the service searches for it.
        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));
        // Simulate that no offer exists for this ride and driver.
        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                InvalidRideStatusException.class,
                () -> rideOfferService.rejectOffer(1L, 1L) // Expect an exception because the requested offer cannot be found.
        );

        // Verify that no offer was saved because the operation failed.
        verify(rideOfferRepository, never()).save(any(RideOffer.class));
    }

    @Test
    void rejectOffer_ShouldThrowException_WhenRideDoesNotExist() {
        // Arrange: Simulate that the requested ride does not exist.
        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());

        // Act & Assert: Expect RideNotFoundException when the ride is missing.
        assertThrows(
                RideNotFoundException.class,
                () -> rideOfferService.rejectOffer(1L, 1L)
        );

        // Verify that the service never searches for an offer.
        verify(rideOfferRepository, never())
                .findByRide_IdAndDriver_Id(1L, 1L);

        // Verify that no offer is saved.
        verify(rideOfferRepository, never()).save(any(RideOffer.class));
    }

    @Test
    void acceptOffer_ShouldAcceptOffer_WhenOfferIsPending() {
        // Arrange: Create another driver for the same ride.
        Driver anotherDriver = new Driver();
        anotherDriver.setId(2L);
        anotherDriver.setStatus(DriverStatus.AVAILABLE);

        // Create another pending offer for the second driver.
        RideOffer anotherOffer = new RideOffer();
        anotherOffer.setRide(ride);
        anotherOffer.setDriver(anotherDriver);
        anotherOffer.setStatus(RideOfferStatus.PENDING);

        // Assign unique IDs because the service compares offer IDs.
        setEntityId(rideOffer, 1L);
        setEntityId(anotherOffer, 2L);

        // Return the existing ride.
        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        // Return the selected pending offer.
        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));

        // Return the selected driver.
        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));

        // Return both pending offers associated with this ride.
        when(rideOfferRepository.findByRide_IdAndStatus(1L, RideOfferStatus.PENDING))
                .thenReturn(List.of(rideOffer, anotherOffer));

        // Return the same offer when saving.
        when(rideOfferRepository.save(any(RideOffer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Accept the selected offer.
        RideOfferResponse response = rideOfferService.acceptOffer(1L, 1L);

        // Assert: Verify the selected offer was accepted.
        assertEquals(RideOfferStatus.ACCEPTED, response.status());
        assertEquals(RideOfferStatus.ACCEPTED, rideOffer.getStatus());

        // Verify the ride was accepted and the driver assigned.
        assertEquals(RideStatus.ACCEPTED, ride.getStatus());
        assertEquals(driver, ride.getDriver());

        // Verify the selected driver is now busy.
        assertEquals(DriverStatus.BUSY, driver.getStatus());

        // Verify the other pending offer expired.
        assertEquals(RideOfferStatus.EXPIRED, anotherOffer.getStatus());

        // Verify the ride and driver were saved.
        verify(rideRepository).save(any(Ride.class));
        verify(driverRepository).save(any(Driver.class));
        verify(rideOfferRepository).saveAll(List.of(rideOffer, anotherOffer));
    }

    // Assign an ID to entities whose IDs are normally generated by the database.
    private void setEntityId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}