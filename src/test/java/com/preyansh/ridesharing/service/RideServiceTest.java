package com.preyansh.ridesharing.service;

import com.preyansh.ridesharing.dto.RideRequest;
import com.preyansh.ridesharing.dto.RideResponse;
import com.preyansh.ridesharing.exception.InvalidRideStatusException;
import com.preyansh.ridesharing.exception.RideNotFoundException;
import com.preyansh.ridesharing.exception.UserNotFoundException;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

        // Create a rider with a predefined ID.
        rider = new User();
        rider.setId(1L);

        // Create a default ride in REQUESTED state.
        ride = new Ride();
        ride.setId(8L);
        ride.setRider(rider);
        ride.setPickupLocation("Baner, Pune");
        ride.setDropoffLocation("Pune Airport");
        ride.setStatus(RideStatus.REQUESTED);
    }

    @Test
    void getRides_shouldReturnAllRidesSuccessfully() {

        // Mock the repository response.
        when(rideRepository.findAll()).thenReturn(List.of(ride));

        // Call the service method.
        List<RideResponse> responses = rideService.getRides();

        // Verify the returned ride details.
        assertEquals(1, responses.size());
        assertEquals(8L, responses.get(0).id());
        assertEquals(1L, responses.get(0).riderId());
        assertNull(responses.get(0).driverId());
        assertEquals("Baner, Pune", responses.get(0).pickupLocation());
        assertEquals("Pune Airport", responses.get(0).dropoffLocation());
        assertEquals(RideStatus.REQUESTED, responses.get(0).status());

        // Verify repository interaction.
        verify(rideRepository).findAll();
    }

    @Test
    void getRides_shouldReturnEmptyListWhenNoRidesExist() {

        // Return an empty list from the repository.
        when(rideRepository.findAll()).thenReturn(List.of());

        // Retrieve all rides.
        List<RideResponse> responses = rideService.getRides();

        // Verify that the result is empty.
        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(rideRepository).findAll();
    }

    @Test
    void getRideById_shouldReturnRideSuccessfully() {

        // Mock the ride lookup.
        when(rideRepository.findById(8L)).thenReturn(Optional.of(ride));

        // Retrieve the ride.
        RideResponse response = rideService.getRideById(8L);

        // Verify the returned ride details.
        assertEquals(8L, response.id());
        assertEquals(1L, response.riderId());
        assertNull(response.driverId());
        assertEquals("Baner, Pune", response.pickupLocation());
        assertEquals("Pune Airport", response.dropoffLocation());
        assertEquals(RideStatus.REQUESTED, response.status());

        verify(rideRepository).findById(8L);
    }

    @Test
    void getRideById_shouldThrowExceptionWhenRideDoesNotExist() {

        // Simulate a ride that does not exist.
        when(rideRepository.findById(999L)).thenReturn(Optional.empty());

        // Verify that the service throws RideNotFoundException.
        assertThrows(
                RideNotFoundException.class,
                () -> rideService.getRideById(999L)
        );

        verify(rideRepository).findById(999L);
    }

    @Test
    void getRidesByRiderId_shouldReturnRidesSuccessfully() {

        // Mock rides belonging to the rider.
        when(rideRepository.findByRiderId(1L)).thenReturn(List.of(ride));

        // Retrieve the rider's rides.
        List<RideResponse> responses = rideService.getRidesByRiderId(1L);

        // Verify the returned ride details.
        assertEquals(1, responses.size());
        assertEquals(8L, responses.get(0).id());
        assertEquals(1L, responses.get(0).riderId());
        assertEquals(RideStatus.REQUESTED, responses.get(0).status());

        verify(rideRepository).findByRiderId(1L);
    }

    @Test
    void getRidesByRiderId_shouldReturnEmptyListWhenNoRidesExist() {

        // Simulate a rider with no rides.
        when(rideRepository.findByRiderId(1L)).thenReturn(List.of());

        // Retrieve the rider's rides.
        List<RideResponse> responses = rideService.getRidesByRiderId(1L);

        // Verify that the result is empty.
        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(rideRepository).findByRiderId(1L);
    }

    @Test
    void getRidesByDriverId_shouldReturnRidesSuccessfully() {

        // Create a driver and assign the ride to that driver.
        Driver driver = new Driver();
        driver.setId(4L);
        driver.setStatus(DriverStatus.BUSY);

        ride.setDriver(driver);
        ride.setStatus(RideStatus.ACCEPTED);

        // Mock rides assigned to the driver.
        when(rideRepository.findByDriverId(4L)).thenReturn(List.of(ride));

        // Retrieve the driver's rides.
        List<RideResponse> responses = rideService.getRidesByDriverId(4L);

        // Verify the returned ride details.
        assertEquals(1, responses.size());
        assertEquals(8L, responses.get(0).id());
        assertEquals(4L, responses.get(0).driverId());
        assertEquals(RideStatus.ACCEPTED, responses.get(0).status());

        verify(rideRepository).findByDriverId(4L);
    }

    @Test
    void getRidesByDriverId_shouldReturnEmptyListWhenNoRidesExist() {

        // Simulate a driver with no assigned rides.
        when(rideRepository.findByDriverId(4L)).thenReturn(List.of());

        // Retrieve the driver's rides.
        List<RideResponse> responses = rideService.getRidesByDriverId(4L);

        // Verify that the result is empty.
        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(rideRepository).findByDriverId(4L);
    }

    @Test
    void getRidesByStatus_shouldReturnMatchingRidesSuccessfully() {

        // Mock rides matching the requested status.
        when(rideRepository.findByStatus(RideStatus.REQUESTED))
                .thenReturn(List.of(ride));

        // Retrieve rides with REQUESTED status.
        List<RideResponse> responses =
                rideService.getRidesByStatus(RideStatus.REQUESTED);

        // Verify the returned ride details.
        assertEquals(1, responses.size());
        assertEquals(8L, responses.get(0).id());
        assertEquals(RideStatus.REQUESTED, responses.get(0).status());

        verify(rideRepository).findByStatus(RideStatus.REQUESTED);
    }

    @Test
    void getRidesByStatus_shouldReturnEmptyListWhenNoMatchingRidesExist() {

        // Simulate no rides matching the requested status.
        when(rideRepository.findByStatus(RideStatus.COMPLETED))
                .thenReturn(List.of());

        // Retrieve completed rides.
        List<RideResponse> responses =
                rideService.getRidesByStatus(RideStatus.COMPLETED);

        // Verify that the result is empty.
        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(rideRepository).findByStatus(RideStatus.COMPLETED);
    }

    @Test
    void createRide_shouldCreateRideSuccessfully() {

        // Prepare the incoming ride request.
        RideRequest request = new RideRequest(
                1L,
                "Baner, Pune",
                "Pune Airport"
        );

        // Mock the rider lookup.
        when(userRepository.findById(1L)).thenReturn(Optional.of(rider));

        // Simulate saving the ride and assigning its generated ID.
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {

            Ride savedRide = invocation.getArgument(0);
            savedRide.setId(8L);

            return savedRide;
        });

        // Create the ride.
        RideResponse response = rideService.createRide(request);

        // Verify the returned ride details.
        assertEquals(8L, response.id());
        assertEquals(1L, response.riderId());
        assertNull(response.driverId());
        assertEquals("Baner, Pune", response.pickupLocation());
        assertEquals("Pune Airport", response.dropoffLocation());
        assertEquals(RideStatus.REQUESTED, response.status());

        // Verify repository interactions.
        verify(userRepository).findById(1L);
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    void createRide_shouldThrowExceptionWhenRiderDoesNotExist() {

        // Prepare a request with a nonexistent rider ID.
        RideRequest request = new RideRequest(
                999L,
                "Baner, Pune",
                "Pune Airport"
        );

        // Simulate a rider that does not exist.
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Verify that ride creation fails.
        assertThrows(
                UserNotFoundException.class,
                () -> rideService.createRide(request)
        );

        // Verify that no ride is saved.
        verify(userRepository).findById(999L);
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void cancelRequestedRide_shouldCancelRideAndExpirePendingOffers() {

        // Mock the locked ride lookup.
        when(rideRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(ride)).thenReturn(ride);

        // Cancel the requested ride.
        RideResponse response = rideService.cancelRide(8L);

        // Verify the ride status.
        assertEquals(RideStatus.CANCELLED, response.status());

        // Verify that pending offers are expired.
        verify(rideOfferService).expirePendingOffers(8L);

        // Verify that the ride is saved.
        verify(rideRepository).save(ride);

        // No driver should be involved in this cancellation.
        verifyNoInteractions(driverRepository);
    }

    @Test
    void cancelAcceptedRide_shouldReleaseDriverAndExpirePendingOffers() {

        // Create a busy driver assigned to the ride.
        Driver driver = new Driver();
        driver.setId(4L);
        driver.setStatus(DriverStatus.BUSY);

        ride.setDriver(driver);
        ride.setStatus(RideStatus.ACCEPTED);

        // Mock repository operations.
        when(rideRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(ride)).thenReturn(ride);

        // Cancel the accepted ride.
        RideResponse response = rideService.cancelRide(8L);

        // Verify that the ride is cancelled.
        assertEquals(RideStatus.CANCELLED, response.status());

        // Verify that the driver becomes available.
        assertEquals(DriverStatus.AVAILABLE, driver.getStatus());

        // Verify that both driver and ride are saved.
        verify(driverRepository).save(driver);
        verify(rideRepository).save(ride);

        // Verify that pending offers are expired.
        verify(rideOfferService).expirePendingOffers(8L);
    }

    @Test
    void cancelAcceptedRideWithoutDriver_shouldCancelWithoutReleasingDriver() {

        // Set the ride to ACCEPTED without assigning a driver.
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setDriver(null);

        when(rideRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(ride)).thenReturn(ride);

        // Cancel the accepted ride.
        RideResponse response = rideService.cancelRide(8L);

        // Verify that the ride is cancelled.
        assertEquals(RideStatus.CANCELLED, response.status());

        // Verify that no driver is saved.
        verifyNoInteractions(driverRepository);

        verify(rideOfferService).expirePendingOffers(8L);
        verify(rideRepository).save(ride);
    }

    @Test
    void cancelRideInInvalidState_shouldThrowException() {

        // Set the ride to COMPLETED, which cannot be cancelled.
        ride.setStatus(RideStatus.COMPLETED);

        when(rideRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(ride));

        // Verify that cancellation throws an exception.
        assertThrows(
                InvalidRideStatusException.class,
                () -> rideService.cancelRide(8L)
        );

        // Verify that no changes are saved.
        verify(rideRepository, never()).save(any(Ride.class));
        verify(rideOfferService, never()).expirePendingOffers(anyLong());
        verifyNoInteractions(driverRepository);
    }

    @Test
    void cancelNonexistentRide_shouldThrowNotFoundException() {

        // Simulate a ride that does not exist.
        when(rideRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        // Verify that cancellation throws RideNotFoundException.
        assertThrows(
                RideNotFoundException.class,
                () -> rideService.cancelRide(999L)
        );

        // Verify that no ride or driver operations occur.
        verify(rideRepository, never()).save(any(Ride.class));
        verifyNoInteractions(driverRepository, rideOfferService);
    }

    @Test
    void startAcceptedRide_shouldMoveToInProgress() {

        // Set the ride to ACCEPTED.
        ride.setStatus(RideStatus.ACCEPTED);

        when(rideRepository.findById(8L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(ride)).thenReturn(ride);

        // Start the accepted ride.
        RideResponse response = rideService.startRide(8L);

        // Verify that the ride moves to IN_PROGRESS.
        assertEquals(RideStatus.IN_PROGRESS, response.status());

        verify(rideRepository).save(ride);
    }

    @Test
    void startRideInInvalidState_shouldThrowException() {

        // A REQUESTED ride cannot be started.
        ride.setStatus(RideStatus.REQUESTED);

        when(rideRepository.findById(8L)).thenReturn(Optional.of(ride));

        // Verify that starting the ride throws an exception.
        assertThrows(
                InvalidRideStatusException.class,
                () -> rideService.startRide(8L)
        );

        // Verify that the ride is not saved.
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void startNonexistentRide_shouldThrowNotFoundException() {

        // Simulate a ride that does not exist.
        when(rideRepository.findById(999L)).thenReturn(Optional.empty());

        // Verify that starting the ride throws RideNotFoundException.
        assertThrows(
                RideNotFoundException.class,
                () -> rideService.startRide(999L)
        );

        // Verify that no ride is saved.
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void completeInProgressRide_shouldCompleteAndReleaseDriver() {

        // Create a busy driver assigned to the ride.
        Driver driver = new Driver();
        driver.setId(4L);
        driver.setStatus(DriverStatus.BUSY);

        ride.setDriver(driver);
        ride.setStatus(RideStatus.IN_PROGRESS);

        // Mock repository operations.
        when(rideRepository.findById(8L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(ride)).thenReturn(ride);

        // Complete the ride.
        RideResponse response = rideService.completeRide(8L);

        // Verify that the ride is completed.
        assertEquals(RideStatus.COMPLETED, response.status());

        // Verify that the driver becomes available.
        assertEquals(DriverStatus.AVAILABLE, driver.getStatus());

        // Verify that both entities are saved.
        verify(driverRepository).save(driver);
        verify(rideRepository).save(ride);
    }

    @Test
    void completeRideInInvalidState_shouldThrowException() {

        // An ACCEPTED ride cannot be completed directly.
        ride.setStatus(RideStatus.ACCEPTED);

        when(rideRepository.findById(8L)).thenReturn(Optional.of(ride));

        // Verify that completion throws an exception.
        assertThrows(
                InvalidRideStatusException.class,
                () -> rideService.completeRide(8L)
        );

        // Verify that neither ride nor driver is saved.
        verify(rideRepository, never()).save(any(Ride.class));
        verifyNoInteractions(driverRepository);
    }

    @Test
    void completeRideWithoutDriver_shouldThrowException() {

        // Set the ride to IN_PROGRESS without an assigned driver.
        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setDriver(null);

        when(rideRepository.findById(8L)).thenReturn(Optional.of(ride));

        // Verify that completion is rejected without a driver.
        assertThrows(
                InvalidRideStatusException.class,
                () -> rideService.completeRide(8L)
        );

        // Verify that neither entity is saved.
        verify(rideRepository, never()).save(any(Ride.class));
        verifyNoInteractions(driverRepository);
    }

    @Test
    void completeNonexistentRide_shouldThrowNotFoundException() {

        // Simulate a ride that does not exist.
        when(rideRepository.findById(999L)).thenReturn(Optional.empty());

        // Verify that completion throws RideNotFoundException.
        assertThrows(
                RideNotFoundException.class,
                () -> rideService.completeRide(999L)
        );

        // Verify that no ride or driver operations occur.
        verify(rideRepository, never()).save(any(Ride.class));
        verifyNoInteractions(driverRepository);
    }
}