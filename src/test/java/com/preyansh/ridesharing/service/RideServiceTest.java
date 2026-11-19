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

        when(rideRepository.findAll()).thenReturn(List.of(ride));

        List<RideResponse> responses = rideService.getRides();

        assertEquals(1, responses.size());
        assertEquals(8L, responses.get(0).id());
        assertEquals(1L, responses.get(0).riderId());
        assertNull(responses.get(0).driverId());
        assertEquals("Baner, Pune", responses.get(0).pickupLocation());
        assertEquals("Pune Airport", responses.get(0).dropoffLocation());
        assertEquals(RideStatus.REQUESTED, responses.get(0).status());

        verify(rideRepository).findAll();
    }

    @Test
    void getRides_shouldReturnEmptyListWhenNoRidesExist() {

        when(rideRepository.findAll()).thenReturn(List.of());

        List<RideResponse> responses = rideService.getRides();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(rideRepository).findAll();
    }

    @Test
    void getRideById_shouldReturnRideSuccessfully() {

        when(rideRepository.findById(8L)).thenReturn(Optional.of(ride));

        RideResponse response = rideService.getRideById(8L);

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

        when(rideRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                RideNotFoundException.class,
                () -> rideService.getRideById(999L)
        );

        verify(rideRepository).findById(999L);
    }

    @Test
    void getRidesByRiderId_shouldReturnRidesSuccessfully() {

        when(rideRepository.findByRiderId(1L)).thenReturn(List.of(ride));

        List<RideResponse> responses = rideService.getRidesByRiderId(1L);

        assertEquals(1, responses.size());
        assertEquals(8L, responses.get(0).id());
        assertEquals(1L, responses.get(0).riderId());
        assertEquals(RideStatus.REQUESTED, responses.get(0).status());

        verify(rideRepository).findByRiderId(1L);
    }

    @Test
    void getRidesByRiderId_shouldReturnEmptyListWhenNoRidesExist() {

        when(rideRepository.findByRiderId(1L)).thenReturn(List.of());

        List<RideResponse> responses = rideService.getRidesByRiderId(1L);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(rideRepository).findByRiderId(1L);
    }

    @Test
    void getRidesByDriverId_shouldReturnRidesSuccessfully() {

        Driver driver = new Driver();
        driver.setId(4L);
        driver.setStatus(DriverStatus.BUSY);

        ride.setDriver(driver);
        ride.setStatus(RideStatus.ACCEPTED);

        when(rideRepository.findByDriverId(4L)).thenReturn(List.of(ride));

        List<RideResponse> responses = rideService.getRidesByDriverId(4L);

        assertEquals(1, responses.size());
        assertEquals(8L, responses.get(0).id());
        assertEquals(4L, responses.get(0).driverId());
        assertEquals(RideStatus.ACCEPTED, responses.get(0).status());

        verify(rideRepository).findByDriverId(4L);
    }

    @Test
    void getRidesByDriverId_shouldReturnEmptyListWhenNoRidesExist() {

        when(rideRepository.findByDriverId(4L)).thenReturn(List.of());

        List<RideResponse> responses = rideService.getRidesByDriverId(4L);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(rideRepository).findByDriverId(4L);
    }

    @Test
    void getRidesByStatus_shouldReturnMatchingRidesSuccessfully() {

        when(rideRepository.findByStatus(RideStatus.REQUESTED))
                .thenReturn(List.of(ride));

        List<RideResponse> responses =
                rideService.getRidesByStatus(RideStatus.REQUESTED);

        assertEquals(1, responses.size());
        assertEquals(8L, responses.get(0).id());
        assertEquals(RideStatus.REQUESTED, responses.get(0).status());

        verify(rideRepository).findByStatus(RideStatus.REQUESTED);
    }

    @Test
    void getRidesByStatus_shouldReturnEmptyListWhenNoMatchingRidesExist() {

        when(rideRepository.findByStatus(RideStatus.COMPLETED))
                .thenReturn(List.of());

        List<RideResponse> responses =
                rideService.getRidesByStatus(RideStatus.COMPLETED);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(rideRepository).findByStatus(RideStatus.COMPLETED);
    }

    @Test
    void createRide_shouldCreateRideSuccessfully() {

        RideRequest request = new RideRequest(
                1L,
                "Baner, Pune",
                "Pune Airport"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(rider));

        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride savedRide = invocation.getArgument(0);
            savedRide.setId(8L);
            return savedRide;
        });

        RideResponse response = rideService.createRide(request);

        assertEquals(8L, response.id());
        assertEquals(1L, response.riderId());
        assertNull(response.driverId());
        assertEquals("Baner, Pune", response.pickupLocation());
        assertEquals("Pune Airport", response.dropoffLocation());
        assertEquals(RideStatus.REQUESTED, response.status());

        verify(userRepository).findById(1L);
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    void createRide_shouldThrowExceptionWhenRiderDoesNotExist() {

        RideRequest request = new RideRequest(
                999L,
                "Baner, Pune",
                "Pune Airport"
        );

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> rideService.createRide(request)
        );

        verify(userRepository).findById(999L);
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void cancelRequestedRide_shouldCancelRideAndExpirePendingOffers() {

        when(rideRepository.findByIdForUpdate(8L))
                .thenReturn(Optional.of(ride));
        when(rideRepository.save(ride)).thenReturn(ride);

        RideResponse response = rideService.cancelRide(8L);

        assertEquals(RideStatus.CANCELLED, response.status());

        verify(rideOfferService).expirePendingOffers(8L);
        verify(rideRepository).save(ride);
        verifyNoInteractions(driverRepository);
    }

    @Test
    void cancelAcceptedRide_shouldReleaseDriverAndExpirePendingOffers() {

        Driver driver = new Driver();
        driver.setId(4L);
        driver.setStatus(DriverStatus.BUSY);

        ride.setDriver(driver);
        ride.setStatus(RideStatus.ACCEPTED);

        when(rideRepository.findByIdForUpdate(8L))
                .thenReturn(Optional.of(ride));
        when(driverRepository.findByIdForUpdate(4L))
                .thenReturn(Optional.of(driver));
        when(rideRepository.save(ride)).thenReturn(ride);

        RideResponse response = rideService.cancelRide(8L);

        assertEquals(RideStatus.CANCELLED, response.status());
        assertEquals(DriverStatus.AVAILABLE, driver.getStatus());

        verify(rideRepository).findByIdForUpdate(8L);
        verify(driverRepository).findByIdForUpdate(4L);
        verify(driverRepository).save(driver);
        verify(rideRepository).save(ride);
        verify(rideOfferService).expirePendingOffers(8L);
    }

    @Test
    void cancelAcceptedRideWithoutDriver_shouldCancelWithoutReleasingDriver() {

        ride.setStatus(RideStatus.ACCEPTED);
        ride.setDriver(null);

        when(rideRepository.findByIdForUpdate(8L))
                .thenReturn(Optional.of(ride));
        when(rideRepository.save(ride)).thenReturn(ride);

        RideResponse response = rideService.cancelRide(8L);

        assertEquals(RideStatus.CANCELLED, response.status());

        verifyNoInteractions(driverRepository);
        verify(rideOfferService).expirePendingOffers(8L);
        verify(rideRepository).save(ride);
    }

    @Test
    void cancelRideInInvalidState_shouldThrowException() {

        ride.setStatus(RideStatus.COMPLETED);

        when(rideRepository.findByIdForUpdate(8L))
                .thenReturn(Optional.of(ride));

        assertThrows(
                InvalidRideStatusException.class,
                () -> rideService.cancelRide(8L)
        );

        verify(rideRepository, never()).save(any(Ride.class));
        verify(rideOfferService, never()).expirePendingOffers(anyLong());
        verifyNoInteractions(driverRepository);
    }

    @Test
    void cancelNonexistentRide_shouldThrowNotFoundException() {

        when(rideRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RideNotFoundException.class,
                () -> rideService.cancelRide(999L)
        );

        verify(rideRepository, never()).save(any(Ride.class));
        verifyNoInteractions(driverRepository, rideOfferService);
    }

    @Test
    void startAcceptedRide_shouldMoveToInProgress() {

        ride.setStatus(RideStatus.ACCEPTED);

        when(rideRepository.findByIdForUpdate(8L))
                .thenReturn(Optional.of(ride));

        RideResponse response = rideService.startRide(8L);

        assertEquals(RideStatus.IN_PROGRESS, response.status());

        verify(rideRepository).findByIdForUpdate(8L);
    }

    @Test
    void startRideInInvalidState_shouldThrowException() {

        ride.setStatus(RideStatus.REQUESTED);

        when(rideRepository.findByIdForUpdate(8L))
                .thenReturn(Optional.of(ride));

        assertThrows(
                InvalidRideStatusException.class,
                () -> rideService.startRide(8L)
        );

        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void startNonexistentRide_shouldThrowNotFoundException() {

        when(rideRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RideNotFoundException.class,
                () -> rideService.startRide(999L)
        );

        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void completeInProgressRide_shouldCompleteAndReleaseDriver() {

        Driver driver = new Driver();
        driver.setId(4L);
        driver.setStatus(DriverStatus.BUSY);

        ride.setDriver(driver);
        ride.setStatus(RideStatus.IN_PROGRESS);

        when(rideRepository.findByIdForUpdate(8L))
                .thenReturn(Optional.of(ride));
        when(driverRepository.findByIdForUpdate(4L))
                .thenReturn(Optional.of(driver));
        when(driverRepository.save(driver)).thenReturn(driver);
        when(rideRepository.save(ride)).thenReturn(ride);

        RideResponse response = rideService.completeRide(8L);

        assertEquals(RideStatus.COMPLETED, response.status());
        assertEquals(DriverStatus.AVAILABLE, driver.getStatus());

        verify(rideRepository).findByIdForUpdate(8L);
        verify(driverRepository).findByIdForUpdate(4L);
        verify(driverRepository).save(driver);
        verify(rideRepository).save(ride);
    }

    @Test
    void completeRideInInvalidState_shouldThrowException() {

        ride.setStatus(RideStatus.ACCEPTED);

        when(rideRepository.findByIdForUpdate(8L))
                .thenReturn(Optional.of(ride));

        assertThrows(
                InvalidRideStatusException.class,
                () -> rideService.completeRide(8L)
        );

        verify(rideRepository, never()).save(any(Ride.class));
        verifyNoInteractions(driverRepository);
    }

    @Test
    void completeRideWithoutDriver_shouldThrowException() {

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setDriver(null);

        when(rideRepository.findByIdForUpdate(8L))
                .thenReturn(Optional.of(ride));

        assertThrows(
                InvalidRideStatusException.class,
                () -> rideService.completeRide(8L)
        );

        verify(rideRepository, never()).save(any(Ride.class));
        verifyNoInteractions(driverRepository);
    }

    @Test
    void completeNonexistentRide_shouldThrowNotFoundException() {

        when(rideRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RideNotFoundException.class,
                () -> rideService.completeRide(999L)
        );

        verify(rideRepository, never()).save(any(Ride.class));
        verifyNoInteractions(driverRepository);
    }
}
