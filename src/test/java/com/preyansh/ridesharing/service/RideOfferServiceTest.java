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
}