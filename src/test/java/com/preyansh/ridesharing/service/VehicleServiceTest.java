package com.preyansh.ridesharing.service;

import com.preyansh.ridesharing.dto.VehicleRequest;
import com.preyansh.ridesharing.dto.VehicleResponse;
import com.preyansh.ridesharing.exception.VehicleAlreadyExistsException;
import com.preyansh.ridesharing.exception.VehicleInUseException;
import com.preyansh.ridesharing.exception.VehicleNotFoundException;
import com.preyansh.ridesharing.model.Vehicle;
import com.preyansh.ridesharing.model.VehicleType;
import com.preyansh.ridesharing.repository.DriverRepository;
import com.preyansh.ridesharing.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private VehicleService vehicleService;

    // GET ALL VEHICLES
    // Verifies that all vehicles are retrieved and correctly converted from Vehicle entities into VehicleResponse DTOs.
    @Test
    void getVehicles_shouldReturnAllVehiclesSuccessfully() {

        Vehicle vehicle1 = createVehicle(
                1L, "MH12AB1234", "Swift", VehicleType.HATCHBACK
        );
        Vehicle vehicle2 = createVehicle(
                2L, "MH14CD5678", "Honda City", VehicleType.SEDAN
        );
        // Mock the repository to return two vehicles.
        when(vehicleRepository.findAll())
                .thenReturn(List.of(vehicle1, vehicle2));
        // Call the service method.
        List<VehicleResponse> result = vehicleService.getVehicles();

        // Verify that both vehicles are returned with correct details.
        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals("MH12AB1234", result.get(0).getRegistrationNumber());
        assertEquals("Swift", result.get(0).getModel());
        assertEquals(VehicleType.HATCHBACK, result.get(0).getVehicleType());

        assertEquals(2L, result.get(1).getId());
        assertEquals("MH14CD5678", result.get(1).getRegistrationNumber());
        assertEquals("Honda City", result.get(1).getModel());
        assertEquals(VehicleType.SEDAN, result.get(1).getVehicleType());

        // Verify that the repository was queried exactly once.
        verify(vehicleRepository, times(1)).findAll();
    }

    // Verifies that an empty list is returned when no vehicles exist.
    @Test
    void getVehicles_shouldReturnEmptyListWhenNoVehiclesExist() {

        // Mock the repository to return an empty list.
        when(vehicleRepository.findAll()).thenReturn(List.of());

        // Retrieve all vehicles.
        List<VehicleResponse> result = vehicleService.getVehicles();

        // Verify that the result is empty.
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(vehicleRepository, times(1)).findAll();
    }

    // GET VEHICLE BY ID
    // Verifies that a vehicle is retrieved successfully using its ID.
    @Test
    void getVehicleById_shouldReturnVehicleSuccessfully() {

        Vehicle vehicle = createVehicle(
                1L, "MH12AB1234", "Swift", VehicleType.HATCHBACK
        );

        // Mock the repository to return the requested vehicle.
        when(vehicleRepository.findById(1L))
                .thenReturn(Optional.of(vehicle));

        // Retrieve the vehicle through the service.
        VehicleResponse result = vehicleService.getVehicleById(1L);

        // Verify that the returned DTO contains the correct details.
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("MH12AB1234", result.getRegistrationNumber());
        assertEquals("Swift", result.getModel());
        assertEquals(VehicleType.HATCHBACK, result.getVehicleType());

        verify(vehicleRepository, times(1)).findById(1L);
    }

    // Verifies that VehicleNotFoundException is thrown when the requested vehicle ID does not exist.
    @Test
    void getVehicleById_shouldThrowExceptionWhenVehicleDoesNotExist() {

        // Mock the repository to indicate that the vehicle is absent.
        when(vehicleRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Verify that the service throws the expected exception.
        assertThrows(
                VehicleNotFoundException.class,
                () -> vehicleService.getVehicleById(99L)
        );

        verify(vehicleRepository, times(1)).findById(99L);
    }

    // CREATE VEHICLE
    // Verifies successful vehicle creation, including normalization of the registration number and trimming of the model name.
    @Test
    void createVehicle_shouldCreateVehicleSuccessfully() {

        VehicleRequest request = createVehicleRequest(
                " mh12ab1234 ", " Swift ", VehicleType.HATCHBACK
        );

        // No existing vehicle has this normalized registration number.
        when(vehicleRepository.existsByRegistrationNumber("MH12AB1234"))
                .thenReturn(false);

        // Return the saved vehicle with its generated ID.
        when(vehicleRepository.save(any(Vehicle.class)))
                .thenAnswer(invocation -> {
                    Vehicle savedVehicle = invocation.getArgument(0);
                    savedVehicle.setId(1L);
                    return savedVehicle;
                });

        // Call the service method.
        VehicleResponse result = vehicleService.createVehicle(request);

        // Verify the response contains normalized and trimmed values.
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("MH12AB1234", result.getRegistrationNumber());
        assertEquals("Swift", result.getModel());
        assertEquals(VehicleType.HATCHBACK, result.getVehicleType());

        // Verify duplicate checking and saving were performed.
        verify(vehicleRepository).existsByRegistrationNumber("MH12AB1234");
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    // Verifies that duplicate registration numbers are rejected before attempting to save a new vehicle.
    @Test
    void createVehicle_shouldThrowExceptionWhenRegistrationNumberAlreadyExists() {

        VehicleRequest request = createVehicleRequest(
                "MH12AB1234", "Swift", VehicleType.HATCHBACK
        );

        // Indicate that the registration number is already registered.
        when(vehicleRepository.existsByRegistrationNumber("MH12AB1234"))
                .thenReturn(true);

        // Verify that the custom duplicate exception is thrown.
        assertThrows(
                VehicleAlreadyExistsException.class,
                () -> vehicleService.createVehicle(request)
        );

        // A duplicate vehicle must never be saved.
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    // UPDATE VEHICLE
    // Verifies successful vehicle update and confirms that the existing vehicle can retain its own registration number.
    @Test
    void updateVehicle_shouldUpdateVehicleSuccessfully() {

        Vehicle existingVehicle = createVehicle(
                1L, "MH12AB1234", "Old Model", VehicleType.HATCHBACK
        );

        VehicleRequest request = createVehicleRequest(
                "MH12AB1234", "New Model", VehicleType.SEDAN
        );

        // Return the existing vehicle when its ID is searched.
        when(vehicleRepository.findById(1L))
                .thenReturn(Optional.of(existingVehicle));

        // No other vehicle uses this registration number.
        when(vehicleRepository.existsByRegistrationNumberAndIdNot(
                "MH12AB1234", 1L
        )).thenReturn(false);

        // Return the updated entity when it is saved.
        when(vehicleRepository.save(any(Vehicle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Update the vehicle.
        VehicleResponse result = vehicleService.updateVehicle(1L, request);

        // Verify that the updated details are returned.
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("MH12AB1234", result.getRegistrationNumber());
        assertEquals("New Model", result.getModel());
        assertEquals(VehicleType.SEDAN, result.getVehicleType());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).existsByRegistrationNumberAndIdNot(
                "MH12AB1234", 1L
        );
        verify(vehicleRepository).save(any(Vehicle.class));
    }


    // Verifies that updating a nonexistent vehicle throws
    // VehicleNotFoundException.
    @Test
    void updateVehicle_shouldThrowExceptionWhenVehicleDoesNotExist() {

        VehicleRequest request = createVehicleRequest(
                "MH12AB1234", "Swift", VehicleType.HATCHBACK
        );

        // Indicate that the requested vehicle ID does not exist.
        when(vehicleRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Verify the expected exception.
        assertThrows(
                VehicleNotFoundException.class,
                () -> vehicleService.updateVehicle(99L, request)
        );

        // No duplicate check or save should happen after the vehicle
        // lookup fails.
        verify(vehicleRepository, never())
                .existsByRegistrationNumberAndIdNot(any(), any());

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    // Verifies that an update is rejected when another vehicle already uses the requested registration number.
    @Test
    void updateVehicle_shouldThrowExceptionWhenRegistrationNumberAlreadyExists() {

        Vehicle existingVehicle = createVehicle(
                1L, "MH12AB1234", "Swift", VehicleType.HATCHBACK
        );

        VehicleRequest request = createVehicleRequest(
                "MH14CD5678", "Honda City", VehicleType.SEDAN
        );

        // Return the vehicle being updated.
        when(vehicleRepository.findById(1L))
                .thenReturn(Optional.of(existingVehicle));

        // Indicate that another vehicle uses the requested registration.
        when(vehicleRepository.existsByRegistrationNumberAndIdNot(
                "MH14CD5678", 1L
        )).thenReturn(true);

        // Verify that the duplicate registration exception is thrown.
        assertThrows(
                VehicleAlreadyExistsException.class,
                () -> vehicleService.updateVehicle(1L, request)
        );

        // The existing vehicle must not be saved with a duplicate number.
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }


    // Verifies that registration numbers are normalized before checking for duplicates during an update.
    @Test
    void updateVehicle_shouldNormalizeRegistrationNumberBeforeCheckingDuplicate() {

        Vehicle existingVehicle = createVehicle(
                1L, "MH12AB1234", "Swift", VehicleType.HATCHBACK
        );

        VehicleRequest request = createVehicleRequest(
                " mh14cd5678 ", "Honda City", VehicleType.SEDAN
        );

        when(vehicleRepository.findById(1L))
                .thenReturn(Optional.of(existingVehicle));

        when(vehicleRepository.existsByRegistrationNumberAndIdNot(
                "MH14CD5678", 1L
        )).thenReturn(false);

        when(vehicleRepository.save(any(Vehicle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VehicleResponse result = vehicleService.updateVehicle(1L, request);

        // Verify that the normalized registration number is stored.
        assertEquals("MH14CD5678", result.getRegistrationNumber());
        assertEquals("Honda City", result.getModel());

        // Verify that the duplicate check used the normalized value.
        verify(vehicleRepository).existsByRegistrationNumberAndIdNot(
                "MH14CD5678", 1L
        );
    }

    // DELETE VEHICLE
    // Verifies that a vehicle is deleted successfully when it exists and is not assigned to any driver.
    @Test
    void deleteVehicle_shouldDeleteVehicleSuccessfully() {

        Vehicle vehicle = createVehicle(
                1L, "MH12AB1234", "Swift", VehicleType.HATCHBACK
        );

        // Return the existing vehicle.
        when(vehicleRepository.findById(1L))
                .thenReturn(Optional.of(vehicle));

        // Indicate that no driver is assigned to this vehicle.
        when(driverRepository.existsByVehicle_Id(1L))
                .thenReturn(false);

        // Perform the deletion.
        vehicleService.deleteVehicle(1L);

        // Verify that the driver assignment was checked.
        verify(driverRepository).existsByVehicle_Id(1L);

        // Verify that the vehicle was deleted.
        verify(vehicleRepository).delete(vehicle);
    }

    // Verifies that deletion fails when the requested vehicle does not exist.
    @Test
    void deleteVehicle_shouldThrowExceptionWhenVehicleDoesNotExist() {

        // Indicate that the vehicle ID cannot be found.
        when(vehicleRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Verify that VehicleNotFoundException is thrown.
        assertThrows(
                VehicleNotFoundException.class,
                () -> vehicleService.deleteVehicle(99L)
        );

        // The service must not check driver assignment or delete
        // anything when the vehicle does not exist.
        verifyNoInteractions(driverRepository);
        verify(vehicleRepository, never()).delete(any(Vehicle.class));
    }

    // Verifies that a vehicle cannot be deleted while it is assigned to a driver.
    @Test
    void deleteVehicle_shouldThrowExceptionWhenVehicleIsAssignedToDriver() {

        Vehicle vehicle = createVehicle(
                1L, "MH12AB1234", "Swift", VehicleType.HATCHBACK
        );

        // Return the existing vehicle.
        when(vehicleRepository.findById(1L))
                .thenReturn(Optional.of(vehicle));

        // Indicate that a driver is currently assigned to this vehicle.
        when(driverRepository.existsByVehicle_Id(1L))
                .thenReturn(true);

        // Verify that the custom VehicleInUseException is thrown.
        assertThrows(
                VehicleInUseException.class,
                () -> vehicleService.deleteVehicle(1L)
        );

        // An assigned vehicle must never be deleted.
        verify(vehicleRepository, never()).delete(any(Vehicle.class));
    }

    // HELPER METHODS
    // Creates a Vehicle entity with the supplied test data.
    private Vehicle createVehicle(
            Long id,
            String registrationNumber,
            String model,
            VehicleType vehicleType
    ) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setRegistrationNumber(registrationNumber);
        vehicle.setModel(model);
        vehicle.setVehicleType(vehicleType);
        return vehicle;
    }

    // Creates a VehicleRequest DTO with the supplied test data.
    private VehicleRequest createVehicleRequest(
            String registrationNumber,
            String model,
            VehicleType vehicleType
    ) {
        VehicleRequest request = new VehicleRequest();
        request.setRegistrationNumber(registrationNumber);
        request.setModel(model);
        request.setVehicleType(vehicleType);
        return request;
    }
}
