package com.preyansh.ridesharing.service;

import com.preyansh.ridesharing.dto.DriverRequest;
import com.preyansh.ridesharing.dto.DriverResponse;
import com.preyansh.ridesharing.exception.DriverNotFoundException;
import com.preyansh.ridesharing.exception.UserNotFoundException;
import com.preyansh.ridesharing.exception.VehicleNotFoundException;
import com.preyansh.ridesharing.model.*;
import com.preyansh.ridesharing.repository.DriverRepository;
import com.preyansh.ridesharing.repository.UserRepository;
import com.preyansh.ridesharing.repository.VehicleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private DriverService driverService;

    private User user;
    private Vehicle vehicle;
    private DriverRequest driverRequest;

    @BeforeEach
    void setUp() {
        // Mock associated entities so the test doesn't need database records.
        user = mock(User.class);
        vehicle = mock(Vehicle.class);

        // Prepare the request sent to createDriver().
        driverRequest = new DriverRequest();
        driverRequest.setUserId(1L);
        driverRequest.setVehicleId(10L);
        driverRequest.setLicenseNumber("MH123456789");
        driverRequest.setStatus(DriverStatus.OFFLINE);
    }

    @Test
    void createDriver_shouldCreateDriverSuccessfully() {

        // Arrange: existing user and vehicle.
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));

        // Mock the saved Driver returned by the repository.
        Driver savedDriver = mock(Driver.class);

        when(savedDriver.getId()).thenReturn(100L);
        when(savedDriver.getUser()).thenReturn(user);
        when(savedDriver.getVehicle()).thenReturn(vehicle);
        when(savedDriver.getLicenseNumber()).thenReturn("MH123456789");
        when(savedDriver.getStatus()).thenReturn(DriverStatus.AVAILABLE);

        when(user.getId()).thenReturn(1L);
        when(vehicle.getId()).thenReturn(10L);

        when(driverRepository.save(any(Driver.class))).thenReturn(savedDriver);

        // Act: execute the service method.
        DriverResponse response = driverService.createDriver(driverRequest);

        // Assert: verify the response DTO.
        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals(10L, response.getVehicleId());
        assertEquals("MH123456789", response.getLicenseNumber());
        assertEquals(DriverStatus.AVAILABLE, response.getStatus());

        // Verify that the user was promoted to DRIVER.
        verify(user).setRole(Role.DRIVER);
        verify(userRepository).save(user);

        // Capture the Driver passed to the repository.
        ArgumentCaptor<Driver> driverCaptor =
                ArgumentCaptor.forClass(Driver.class);

        verify(driverRepository).save(driverCaptor.capture());

        Driver capturedDriver = driverCaptor.getValue();

        // Verify the Driver entity was populated correctly.
        assertSame(user, capturedDriver.getUser());
        assertSame(vehicle, capturedDriver.getVehicle());
        assertEquals("MH123456789", capturedDriver.getLicenseNumber());

        // The service controls the initial status.
        assertEquals(DriverStatus.AVAILABLE, capturedDriver.getStatus());
    }

    @Test
    void createDriver_shouldThrowExceptionWhenUserDoesNotExist() {

        // Arrange: user ID is not present.
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert.
        assertThrows(
                UserNotFoundException.class,
                () -> driverService.createDriver(driverRequest)
        );

        // Vehicle lookup and driver creation should never happen.
        verifyNoInteractions(vehicleRepository);
        verifyNoInteractions(driverRepository);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createDriver_shouldThrowExceptionWhenVehicleDoesNotExist() {

        // Arrange: user exists, but vehicle doesn't.
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(vehicleRepository.findById(10L)).thenReturn(Optional.empty());

        // Act + Assert.
        assertThrows(
                VehicleNotFoundException.class,
                () -> driverService.createDriver(driverRequest)
        );

        // User role is updated before vehicle lookup in the current service.
        verify(user).setRole(Role.DRIVER);
        verify(userRepository).save(user);

        // Driver should not be created.
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    void getDriverById_shouldReturnDriverSuccessfully() {

        // Arrange: create mock entities.
        Driver driver = mock(Driver.class);
        User user = mock(User.class);
        Vehicle vehicle = mock(Vehicle.class);

        when(driverRepository.findById(1L))
                .thenReturn(Optional.of(driver));

        when(driver.getId()).thenReturn(1L);
        when(driver.getUser()).thenReturn(user);
        when(driver.getVehicle()).thenReturn(vehicle);
        when(driver.getLicenseNumber()).thenReturn("MH123456789");
        when(driver.getStatus()).thenReturn(DriverStatus.AVAILABLE);

        when(user.getId()).thenReturn(10L);
        when(vehicle.getId()).thenReturn(20L);

        // Act: call the service method.
        DriverResponse response = driverService.getDriverById(1L);

        // Assert: verify the returned response.
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(10L, response.getUserId());
        assertEquals(20L, response.getVehicleId());
        assertEquals("MH123456789", response.getLicenseNumber());
        assertEquals(DriverStatus.AVAILABLE, response.getStatus());

        verify(driverRepository).findById(1L);
    }

    @Test
    void getDriverById_shouldThrowExceptionWhenDriverDoesNotExist() {

        // Arrange: driver ID doesn't exist.
        when(driverRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act + Assert.
        assertThrows(
                DriverNotFoundException.class,
                () -> driverService.getDriverById(999L)
        );

        verify(driverRepository).findById(999L);
    }


}
