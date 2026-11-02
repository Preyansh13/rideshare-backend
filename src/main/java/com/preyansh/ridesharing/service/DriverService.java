package com.preyansh.ridesharing.service;

import com.preyansh.ridesharing.dto.DriverRequest;
import com.preyansh.ridesharing.dto.DriverResponse;
import com.preyansh.ridesharing.exception.DriverNotFoundException;
import com.preyansh.ridesharing.exception.UserNotFoundException;
import com.preyansh.ridesharing.exception.VehicleAlreadyAssignedException;
import com.preyansh.ridesharing.exception.VehicleNotFoundException;
import com.preyansh.ridesharing.model.*;
import com.preyansh.ridesharing.repository.DriverRepository;
import com.preyansh.ridesharing.repository.UserRepository;
import com.preyansh.ridesharing.repository.VehicleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    // Constructor injection.
    public DriverService(
            DriverRepository driverRepository,
            UserRepository userRepository,
            VehicleRepository vehicleRepository) {
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
    }

    // 1. CREATE DRIVER
    @Transactional
    public DriverResponse createDriver(DriverRequest driverRequest) {

        // Find the existing user.
        User user = userRepository.findById(driverRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException(
                        "User with id " + driverRequest.getUserId() + " not found"
                ));

        // Promote the user to DRIVER role.
        user.setRole(Role.DRIVER);
        userRepository.save(user);

        // Find the existing vehicle.
        Vehicle vehicle = vehicleRepository.findById(driverRequest.getVehicleId())
                .orElseThrow(() -> new VehicleNotFoundException(
                        "Vehicle with id " + driverRequest.getVehicleId() + " not found"
                ));

        // Prevent assigning a vehicle that already belongs to another driver.
        if (driverRepository.existsByVehicle_Id(driverRequest.getVehicleId())) {
            throw new VehicleAlreadyAssignedException(
                    "Vehicle with id " + driverRequest.getVehicleId() + " is already assigned to another driver"
            );
        }

        // Create and populate the Driver entity.
        Driver driver = new Driver();
        driver.setUser(user);
        driver.setVehicle(vehicle);
        driver.setLicenseNumber(driverRequest.getLicenseNumber());

        // Initial status is controlled by the backend.
        driver.setStatus(DriverStatus.AVAILABLE);

        // Save the driver.
        Driver savedDriver = driverRepository.save(driver);

        return mapToDriverResponse(savedDriver);
    }

    // 2. GET DRIVER BY ID
    @Transactional(readOnly = true)
    public DriverResponse getDriverById(Long driverId) {

        // Retrieve the driver or throw an exception.
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException(
                        "Driver with id " + driverId + " not found"
                ));

        return mapToDriverResponse(driver);
    }

    // 3. GET ALL DRIVERS
    @Transactional(readOnly = true)
    public List<DriverResponse> getAllDrivers() {

        // Retrieve all drivers and convert each entity into a response DTO.
        return driverRepository.findAll()
                .stream()
                .map(this::mapToDriverResponse)
                .toList();
    }

    // 4. UPDATE DRIVER PROFILE
    @Transactional
    public DriverResponse updateDriver(
            Long driverId,
            DriverRequest driverRequest) {

        // Find the driver that needs to be updated.
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException(
                        "Driver with id " + driverId + " not found"
                ));

        // Update the license number.
        driver.setLicenseNumber(driverRequest.getLicenseNumber());

        // Find the requested vehicle.
        Vehicle vehicle = vehicleRepository.findById(driverRequest.getVehicleId())
                .orElseThrow(() -> new VehicleNotFoundException(
                        "Vehicle with id " + driverRequest.getVehicleId() + " not found"
                ));

        // Check whether the requested vehicle is assigned to another driver.
        boolean vehicleAlreadyAssigned =
                driverRepository.existsByVehicle_Id(driverRequest.getVehicleId());

        // Allow the current driver to retain their existing vehicle.
        boolean sameVehicle =
                driver.getVehicle().getId().equals(driverRequest.getVehicleId());

        if (vehicleAlreadyAssigned && !sameVehicle) {
            throw new VehicleAlreadyAssignedException(
                    "Vehicle with id " + driverRequest.getVehicleId() + " is already assigned to another driver"
            );
        }

        // Update the vehicle association.
        driver.setVehicle(vehicle);

        // Save and return the updated driver.
        Driver updatedDriver = driverRepository.save(driver);

        return mapToDriverResponse(updatedDriver);
    }

    // 5. UPDATE DRIVER STATUS
    @Transactional
    public DriverResponse updateDriverStatus(
            Long driverId,
            DriverStatus status) {

        // Find the driver.
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException(
                        "Driver with id " + driverId + " not found"
                ));

        // Update the operational status.
        driver.setStatus(status);

        // Save and return the updated driver.
        Driver updatedDriver = driverRepository.save(driver);

        return mapToDriverResponse(updatedDriver);
    }

    // PRIVATE HELPER: MAP ENTITY TO RESPONSE DTO
    private DriverResponse mapToDriverResponse(Driver driver) {

        DriverResponse response = new DriverResponse();

        response.setId(driver.getId());
        response.setUserId(driver.getUser().getId());
        response.setVehicleId(driver.getVehicle().getId());
        response.setLicenseNumber(driver.getLicenseNumber());
        response.setStatus(driver.getStatus());

        return response;
    }
}
