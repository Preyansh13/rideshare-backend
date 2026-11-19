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

    @Mock
    private RideOfferRepository rideOfferRepository;

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private RideOfferService rideOfferService;

    private Ride ride;
    private Driver driver;
    private User user;
    private RideOffer rideOffer;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setName("Test Driver");
        user.setEmail("driver@test.com");
        user.setPhone("9876543210");
        user.setRole(Role.DRIVER);

        driver = new Driver();
        driver.setId(1L);
        driver.setUser(user);
        driver.setLicenseNumber("DL123456");
        driver.setStatus(DriverStatus.AVAILABLE);

        ride = new Ride();
        ride.setId(1L);
        ride.setPickupLocation("Pune");
        ride.setDropoffLocation("Mumbai");
        ride.setStatus(RideStatus.REQUESTED);

        rideOffer = new RideOffer();
        rideOffer.setRide(ride);
        rideOffer.setDriver(driver);
        rideOffer.setStatus(RideOfferStatus.PENDING);

        setEntityId(rideOffer, 1L);
    }

    @Test
    void createOffer_shouldCreateOffer_whenRideAndDriverAreValid() {

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));

        when(rideOfferRepository.existsByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(false);

        when(rideOfferRepository.save(any(RideOffer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RideOfferResponse response = rideOfferService.createOffer(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.rideId());
        assertEquals(1L, response.driverId());
        assertEquals(RideOfferStatus.PENDING, response.status());

        verify(rideRepository).findByIdForUpdate(1L);
        verify(driverRepository).findByIdForUpdate(1L);
        verify(rideOfferRepository).save(any(RideOffer.class));
    }

    @Test
    void createOffer_shouldThrowException_whenRideDoesNotExist() {

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RideNotFoundException.class,
                () -> rideOfferService.createOffer(1L, 1L)
        );

        verifyNoInteractions(driverRepository, rideOfferRepository);
    }

    @Test
    void createOffer_shouldThrowException_whenRideIsNotRequested() {

        ride.setStatus(RideStatus.ACCEPTED);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        assertThrows(
                InvalidRideStatusException.class,
                () -> rideOfferService.createOffer(1L, 1L)
        );

        verifyNoInteractions(driverRepository, rideOfferRepository);
    }

    @Test
    void createOffer_shouldThrowException_whenRideAlreadyHasDriver() {

        ride.setDriver(driver);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        assertThrows(
                InvalidRideStatusException.class,
                () -> rideOfferService.createOffer(1L, 1L)
        );

        verifyNoInteractions(driverRepository, rideOfferRepository);
    }

    @Test
    void createOffer_shouldThrowException_whenDriverDoesNotExist() {

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                DriverNotFoundException.class,
                () -> rideOfferService.createOffer(1L, 1L)
        );

        verify(rideOfferRepository, never()).save(any(RideOffer.class));
    }

    @Test
    void createOffer_shouldThrowException_whenDriverIsUnavailable() {

        driver.setStatus(DriverStatus.BUSY);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));

        assertThrows(
                InvalidDriverStatusException.class,
                () -> rideOfferService.createOffer(1L, 1L)
        );

        verify(rideOfferRepository, never()).save(any(RideOffer.class));
    }

    @Test
    void createOffer_shouldThrowException_whenDriverIsOffline() {

        driver.setStatus(DriverStatus.OFFLINE);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));

        assertThrows(
                InvalidDriverStatusException.class,
                () -> rideOfferService.createOffer(1L, 1L)
        );

        verify(rideOfferRepository, never()).save(any(RideOffer.class));
    }

    @Test
    void createOffer_shouldThrowException_whenUserIsNotDriver() {

        user.setRole(Role.RIDER);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));

        assertThrows(
                InvalidDriverStatusException.class,
                () -> rideOfferService.createOffer(1L, 1L)
        );

        verify(rideOfferRepository, never()).save(any(RideOffer.class));
    }

    @Test
    void createOffer_shouldThrowException_whenOfferAlreadyExists() {

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));

        when(rideOfferRepository.existsByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(true);

        assertThrows(
                InvalidRideStatusException.class,
                () -> rideOfferService.createOffer(1L, 1L)
        );

        verify(rideOfferRepository, never()).save(any(RideOffer.class));
    }

    @Test
    void rejectOffer_shouldRejectOffer_whenOfferIsPending() {

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));

        when(rideOfferRepository.save(any(RideOffer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RideOfferResponse response = rideOfferService.rejectOffer(1L, 1L);

        assertEquals(RideOfferStatus.REJECTED, response.status());
        assertEquals(RideStatus.REQUESTED, ride.getStatus());
        assertEquals(DriverStatus.AVAILABLE, driver.getStatus());

        verify(rideOfferRepository).save(rideOffer);

        // Rejection does not need to lock or modify the driver.
        verifyNoInteractions(driverRepository);
    }

    @Test
    void rejectOffer_shouldThrowException_whenRideDoesNotExist() {

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RideNotFoundException.class,
                () -> rideOfferService.rejectOffer(1L, 1L)
        );

        verify(rideOfferRepository, never())
                .findByRide_IdAndDriver_Id(1L, 1L);
        verifyNoInteractions(driverRepository);
    }

    @Test
    void rejectOffer_shouldThrowException_whenRideIsNotRequested() {

        ride.setStatus(RideStatus.ACCEPTED);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        assertThrows(
                InvalidRideStatusException.class,
                () -> rideOfferService.rejectOffer(1L, 1L)
        );

        verifyNoInteractions(rideOfferRepository, driverRepository);
    }

    @Test
    void rejectOffer_shouldThrowException_whenRideAlreadyHasDriver() {

        ride.setDriver(driver);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        assertThrows(
                InvalidRideStatusException.class,
                () -> rideOfferService.rejectOffer(1L, 1L)
        );

        verifyNoInteractions(rideOfferRepository, driverRepository);
    }

    @Test
    void rejectOffer_shouldThrowException_whenOfferDoesNotExist() {

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidRideStatusException.class,
                () -> rideOfferService.rejectOffer(1L, 1L)
        );

        verify(rideOfferRepository, never()).save(any(RideOffer.class));
        verifyNoInteractions(driverRepository);
    }

    @Test
    void rejectOffer_shouldThrowException_whenOfferIsAlreadyProcessed() {

        rideOffer.setStatus(RideOfferStatus.REJECTED);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));

        assertThrows(
                InvalidRideStatusException.class,
                () -> rideOfferService.rejectOffer(1L, 1L)
        );

        verifyNoInteractions(driverRepository);
        verify(rideOfferRepository, never()).save(any(RideOffer.class));
    }

    @Test
    void rejectOffer_shouldThrowException_whenUserIsNotDriver() {

        user.setRole(Role.RIDER);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));

        assertThrows(
                InvalidDriverStatusException.class,
                () -> rideOfferService.rejectOffer(1L, 1L)
        );

        verifyNoInteractions(driverRepository);
        verify(rideOfferRepository, never()).save(any(RideOffer.class));
    }

    @Test
    void acceptOffer_shouldAcceptOffer_whenOfferIsPending() {

        Driver anotherDriver = new Driver();
        anotherDriver.setId(2L);
        anotherDriver.setStatus(DriverStatus.AVAILABLE);

        RideOffer anotherOffer = new RideOffer();
        anotherOffer.setRide(ride);
        anotherOffer.setDriver(anotherDriver);
        anotherOffer.setStatus(RideOfferStatus.PENDING);
        setEntityId(anotherOffer, 2L);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));

        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));

        when(rideOfferRepository.findByRide_IdAndStatus(
                1L, RideOfferStatus.PENDING))
                .thenReturn(List.of(rideOffer, anotherOffer));

        RideOfferResponse response = rideOfferService.acceptOffer(1L, 1L);

        assertEquals(RideOfferStatus.ACCEPTED, response.status());
        assertEquals(RideOfferStatus.ACCEPTED, rideOffer.getStatus());

        assertEquals(RideStatus.ACCEPTED, ride.getStatus());
        assertEquals(driver, ride.getDriver());

        assertEquals(DriverStatus.BUSY, driver.getStatus());
        assertEquals(RideOfferStatus.EXPIRED, anotherOffer.getStatus());

        verify(rideRepository).save(ride);
        verify(driverRepository).save(driver);
        verify(rideOfferRepository).saveAll(List.of(rideOffer, anotherOffer));
        verify(rideOfferRepository).save(rideOffer);
    }

    @Test
    void acceptOffer_shouldThrowException_whenRideDoesNotExist() {

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RideNotFoundException.class,
                () -> rideOfferService.acceptOffer(1L, 1L)
        );

        verifyNoInteractions(rideOfferRepository, driverRepository);
    }

    @Test
    void acceptOffer_shouldThrowException_whenRideIsNotRequested() {

        ride.setStatus(RideStatus.ACCEPTED);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        assertThrows(
                InvalidRideStatusException.class,
                () -> rideOfferService.acceptOffer(1L, 1L)
        );

        verifyNoInteractions(rideOfferRepository, driverRepository);
    }

    @Test
    void acceptOffer_shouldThrowException_whenDriverAlreadyAssigned() {

        ride.setDriver(driver);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        assertThrows(
                InvalidRideStatusException.class,
                () -> rideOfferService.acceptOffer(1L, 1L)
        );

        verifyNoInteractions(rideOfferRepository, driverRepository);
    }

    @Test
    void acceptOffer_shouldThrowException_whenOfferDoesNotExist() {

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidRideStatusException.class,
                () -> rideOfferService.acceptOffer(1L, 1L)
        );

        verifyNoInteractions(driverRepository);
    }

    @Test
    void acceptOffer_shouldThrowException_whenOfferIsAlreadyProcessed() {

        rideOffer.setStatus(RideOfferStatus.REJECTED);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));

        assertThrows(
                InvalidRideStatusException.class,
                () -> rideOfferService.acceptOffer(1L, 1L)
        );

        verifyNoInteractions(driverRepository);
    }

    @Test
    void acceptOffer_shouldThrowException_whenDriverDoesNotExist() {

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));

        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                DriverNotFoundException.class,
                () -> rideOfferService.acceptOffer(1L, 1L)
        );

        verify(rideOfferRepository, never()).save(any(RideOffer.class));
        verify(rideOfferRepository, never()).saveAll(anyList());
        verify(rideRepository, never()).save(any(Ride.class));
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    void acceptOffer_shouldThrowException_whenDriverIsUnavailable() {

        driver.setStatus(DriverStatus.BUSY);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));

        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));

        assertThrows(
                InvalidDriverStatusException.class,
                () -> rideOfferService.acceptOffer(1L, 1L)
        );

        verify(rideOfferRepository, never()).save(any(RideOffer.class));
        verify(rideOfferRepository, never()).saveAll(anyList());
        verify(rideRepository, never()).save(any(Ride.class));
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    void acceptOffer_shouldThrowException_whenDriverIsOffline() {

        driver.setStatus(DriverStatus.OFFLINE);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));

        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));

        assertThrows(
                InvalidDriverStatusException.class,
                () -> rideOfferService.acceptOffer(1L, 1L)
        );

        verify(rideRepository, never()).save(any(Ride.class));
        verify(rideOfferRepository, never()).save(any(RideOffer.class));
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    void acceptOffer_shouldThrowException_whenUserIsNotDriver() {

        user.setRole(Role.RIDER);

        when(rideRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ride));

        when(rideOfferRepository.findByRide_IdAndDriver_Id(1L, 1L))
                .thenReturn(Optional.of(rideOffer));

        when(driverRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(driver));

        assertThrows(
                InvalidDriverStatusException.class,
                () -> rideOfferService.acceptOffer(1L, 1L)
        );

        verify(rideOfferRepository, never()).save(any(RideOffer.class));
        verify(rideOfferRepository, never()).saveAll(anyList());
        verify(rideRepository, never()).save(any(Ride.class));
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    void expirePendingOffers_shouldExpireAllPendingOffers_whenOffersExist() {

        RideOffer anotherOffer = new RideOffer();
        anotherOffer.setRide(ride);
        anotherOffer.setDriver(driver);
        anotherOffer.setStatus(RideOfferStatus.PENDING);

        List<RideOffer> pendingOffers = List.of(rideOffer, anotherOffer);

        when(rideOfferRepository.findByRide_IdAndStatus(
                1L, RideOfferStatus.PENDING))
                .thenReturn(pendingOffers);

        rideOfferService.expirePendingOffers(1L);

        assertEquals(RideOfferStatus.EXPIRED, rideOffer.getStatus());
        assertEquals(RideOfferStatus.EXPIRED, anotherOffer.getStatus());

        verify(rideOfferRepository).saveAll(pendingOffers);
    }

    @Test
    void expirePendingOffers_shouldNotSave_whenNoPendingOffersExist() {

        when(rideOfferRepository.findByRide_IdAndStatus(
                1L, RideOfferStatus.PENDING))
                .thenReturn(List.of());

        rideOfferService.expirePendingOffers(1L);

        verify(rideOfferRepository, never()).saveAll(anyList());
    }

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
