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
    void createOffer_shouldCreateOffer_whenRideAndDriverAreValid() {

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
    void createOffer_shouldThrowException_whenOfferAlreadyExists() {

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
    void createOffer_shouldThrowException_whenDriverIsUnavailable() {

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
    void rejectOffer_shouldRejectOffer_whenOfferIsPending() {

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
    void rejectOffer_shouldThrowException_whenOfferIsAlreadyProcessed() {

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
    void rejectOffer_shouldThrowException_whenOfferDoesNotExist() {

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
    void rejectOffer_shouldThrowException_whenRideDoesNotExist() {
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
    void acceptOffer_shouldAcceptOffer_whenOfferIsPending() {
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

    @Test
    void acceptOffer_shouldThrowException_whenRideDoesNotExist() {
        // Arrange: Simulate a ride that does not exist.
        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());

        // Act & Assert: Expect RideNotFoundException.
        assertThrows(RideNotFoundException.class,
                () -> rideOfferService.acceptOffer(1L, 1L));

        // Verify that processing stops after the ride lookup.
        verify(rideRepository).findByIdForUpdate(1L);
        verifyNoInteractions(rideOfferRepository, driverRepository);
    }

    @Test
    void acceptOffer_shouldThrowException_whenRideIsNotRequested() {
        // Arrange: Change the ride status to ACCEPTED.
        ride.setStatus(RideStatus.ACCEPTED);
        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        // Act & Assert: Expect InvalidRideStatusException.
        assertThrows(InvalidRideStatusException.class,
                () -> rideOfferService.acceptOffer(1L, 1L));

        // Verify that offer and driver lookups never happen.
        verify(rideOfferRepository, never())
                .findByRide_IdAndDriver_Id(anyLong(), anyLong());
        verifyNoInteractions(driverRepository);
    }

    @Test
    void acceptOffer_shouldThrowException_whenDriverAlreadyAssigned() {
        // Arrange: Create an available driver and assign them to the ride.
        Driver assignedDriver = new Driver();
        assignedDriver.setId(1L);
        assignedDriver.setStatus(DriverStatus.AVAILABLE);

        // Keep the ride REQUESTED but assign an existing driver.
        ride.setStatus(RideStatus.REQUESTED);
        ride.setDriver(assignedDriver);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        // Act & Assert: Expect InvalidRideStatusException.
        assertThrows(InvalidRideStatusException.class,
                () -> rideOfferService.acceptOffer(1L, 1L));

        // Verify that no offer or driver lookup happens.
        verifyNoInteractions(rideOfferRepository, driverRepository);
    }

    @Test
    void acceptOffer_shouldThrowException_whenOfferDoesNotExist() {
        // Arrange: Ensure the ride is available for acceptance.
        ride.setStatus(RideStatus.REQUESTED);
        ride.setDriver(null);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        // No offer exists for this ride and driver.
        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert: Expect InvalidRideStatusException.
        assertThrows(InvalidRideStatusException.class,
                () -> rideOfferService.acceptOffer(1L, 1L));

        // Verify that driver lookup never happens.
        verify(rideOfferRepository)
                .findByRide_IdAndDriver_Id(1L, 1L);
        verifyNoInteractions(driverRepository);
    }

    @Test
    void acceptOffer_shouldThrowException_whenOfferIsAlreadyProcessed() {
        // Arrange: Set ride status to REQUESTED.
        ride.setStatus(RideStatus.REQUESTED);
        ride.setDriver(null);
        // Arrange: Set offer status to REJECTED.
        rideOffer.setStatus(RideOfferStatus.REJECTED);

        // Mock ride repository.
        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));
        // Mock offer repository.
        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));

        // Act & Assert: Expect InvalidRideStatusException.
        assertThrows(InvalidRideStatusException.class,
                () -> rideOfferService.acceptOffer(1L, 1L));

        // Verify driverRepository has no interactions.
        verifyNoInteractions(driverRepository);
    }

    @Test
    void acceptOffer_shouldThrowException_whenDriverDoesNotExist() {
        // Arrange: Keep the ride available for acceptance.
        ride.setStatus(RideStatus.REQUESTED);
        ride.setDriver(null);
        rideOffer.setStatus(RideOfferStatus.PENDING);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));
        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));
        // Simulate a driver that does not exist.
        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());

        // Act & Assert: Expect DriverNotFoundException.
        assertThrows(DriverNotFoundException.class,
                () -> rideOfferService.acceptOffer(1L, 1L));

        // Verify no database save operations happen.
        verify(rideOfferRepository, never()).save(any(RideOffer.class));
        verify(rideOfferRepository, never()).saveAll(anyList());
        verify(rideRepository, never()).save(any(Ride.class));
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    void acceptOffer_shouldThrowException_whenDriverIsUnavailable() {
        // Arrange: Keep the ride and offer valid.
        ride.setStatus(RideStatus.REQUESTED);
        ride.setDriver(null);
        rideOffer.setStatus(RideOfferStatus.PENDING);
        // Make the driver unavailable.
        driver.setStatus(DriverStatus.BUSY);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));
        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));
        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));

        // Act & Assert: Expect InvalidDriverStatusException.
        assertThrows(InvalidDriverStatusException.class,
                () -> rideOfferService.acceptOffer(1L, 1L));

        // Verify no database save operations happen.
        verify(rideOfferRepository, never()).save(any(RideOffer.class));
        verify(rideOfferRepository, never()).saveAll(anyList());
        verify(rideRepository, never()).save(any(Ride.class));
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    void acceptOffer_shouldThrowException_whenUserIsNotDriver() {
        // Arrange: Keep the ride and offer valid.
        ride.setStatus(RideStatus.REQUESTED);
        ride.setDriver(null);
        rideOffer.setStatus(RideOfferStatus.PENDING);
        // Driver is available, but the associated user has RIDER role.
        driver.setStatus(DriverStatus.AVAILABLE);

        User riderUser = new User();
        riderUser.setRole(Role.RIDER);
        driver.setUser(riderUser);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));
        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));
        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));

        // Act & Assert: Expect InvalidDriverStatusException.
        assertThrows(InvalidDriverStatusException.class,
                () -> rideOfferService.acceptOffer(1L, 1L));

        // Verify no database save operations happen.
        verify(rideOfferRepository, never()).save(any(RideOffer.class));
        verify(rideOfferRepository, never()).saveAll(anyList());
        verify(rideRepository, never()).save(any(Ride.class));
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    void createOffer_shouldThrowException_whenRideDoesNotExist() {
        // Arrange: Simulate a ride that does not exist.
        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());

        // Act & Assert.
        assertThrows(
                RideNotFoundException.class,
                () -> rideOfferService.createOffer(1L, 1L)
        );

        // Verify that driver lookup never happens.
        verifyNoInteractions(driverRepository, rideOfferRepository);
    }

    @Test
    void createOffer_shouldThrowException_whenRideIsNotRequested() {
        // Arrange: Change the ride status.
        ride.setStatus(RideStatus.ACCEPTED);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        // Act & Assert.
        assertThrows(
                InvalidRideStatusException.class,
                () -> rideOfferService.createOffer(1L, 1L)
        );

        verifyNoInteractions(driverRepository, rideOfferRepository);
    }

    @Test
    void createOffer_shouldThrowException_whenDriverDoesNotExist() {
        // Arrange.
        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());

        // Act & Assert.
        assertThrows(
                DriverNotFoundException.class,
                () -> rideOfferService.createOffer(1L, 1L)
        );

        verify(rideOfferRepository, never()).save(any(RideOffer.class));
    }

    @Test
    void createOffer_shouldThrowException_whenUserIsNotDriver() {
        // Arrange: Driver is available, but user has RIDER role.
        user.setRole(Role.RIDER);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));

        // Act & Assert.
        assertThrows(
                InvalidDriverStatusException.class,
                () -> rideOfferService.createOffer(1L, 1L)
        );

        verify(rideOfferRepository, never()).save(any(RideOffer.class));
    }

    @Test
    void createOffer_shouldThrowException_whenRideAlreadyHasDriver() {
        // Arrange.
        ride.setDriver(driver);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        // Act & Assert.
        assertThrows(
                InvalidRideStatusException.class,
                () -> rideOfferService.createOffer(1L, 1L)
        );

        verifyNoInteractions(driverRepository, rideOfferRepository);
    }

    @Test
    void rejectOffer_shouldThrowException_whenRideIsNotRequested() {
        // Arrange.
        ride.setStatus(RideStatus.ACCEPTED);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        // Act & Assert.
        assertThrows(
                InvalidRideStatusException.class,
                () -> rideOfferService.rejectOffer(1L, 1L)
        );

        verifyNoInteractions(rideOfferRepository, driverRepository);
    }

    @Test
    void rejectOffer_shouldThrowException_whenDriverDoesNotExist() {
        // Arrange.
        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));

        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());

        // Act & Assert.
        assertThrows(
                DriverNotFoundException.class,
                () -> rideOfferService.rejectOffer(1L, 1L)
        );

        verify(rideOfferRepository, never()).save(any(RideOffer.class));
    }

    @Test
    void rejectOffer_shouldThrowException_whenUserIsNotDriver() {
        // Arrange.
        user.setRole(Role.RIDER);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));

        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));

        // Act & Assert.
        assertThrows(
                InvalidDriverStatusException.class,
                () -> rideOfferService.rejectOffer(1L, 1L)
        );

        verify(rideOfferRepository, never()).save(any(RideOffer.class));
    }

    @Test
    void expirePendingOffers_shouldExpireAllPendingOffers_whenOffersExist() {
        // Arrange: Create another pending offer.
        RideOffer anotherOffer = new RideOffer();
        anotherOffer.setRide(ride);
        anotherOffer.setDriver(driver);
        anotherOffer.setStatus(RideOfferStatus.PENDING);

        List<RideOffer> pendingOffers = List.of(rideOffer, anotherOffer);

        when(rideOfferRepository.findByRide_IdAndStatus(
                1L, RideOfferStatus.PENDING
        )).thenReturn(pendingOffers);

        // Act.
        rideOfferService.expirePendingOffers(1L);

        // Assert.
        assertEquals(RideOfferStatus.EXPIRED, rideOffer.getStatus());
        assertEquals(RideOfferStatus.EXPIRED, anotherOffer.getStatus());

        verify(rideOfferRepository).saveAll(pendingOffers);
    }

    @Test
    void expirePendingOffers_shouldSaveEmptyList_whenNoPendingOffersExist() {
        // Arrange.
        when(rideOfferRepository.findByRide_IdAndStatus(
                1L, RideOfferStatus.PENDING
        )).thenReturn(List.of());

        // Act.
        rideOfferService.expirePendingOffers(1L);

        // Assert.
        verify(rideOfferRepository).saveAll(List.of());
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