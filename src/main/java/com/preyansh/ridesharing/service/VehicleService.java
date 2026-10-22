package com.preyansh.ridesharing.service;

import com.preyansh.ridesharing.dto.VehicleRequest;
import com.preyansh.ridesharing.dto.VehicleResponse;
import com.preyansh.ridesharing.exception.VehicleAlreadyExistsException;
import com.preyansh.ridesharing.exception.VehicleInUseException;
import com.preyansh.ridesharing.exception.VehicleNotFoundException;
import com.preyansh.ridesharing.model.Vehicle;
import com.preyansh.ridesharing.repository.DriverRepository;
import com.preyansh.ridesharing.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Handles vehicle-related business logic.
@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    // Injects the required repositories.
    public VehicleService(VehicleRepository vehicleRepository,
                          DriverRepository driverRepository) {
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    // Retrieves all vehicles.
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehicles() {
        return vehicleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Retrieves a vehicle by ID.
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        return toResponse(findVehicleOrThrow(id));
    }

    // Registers a new vehicle.
    @Transactional
    public VehicleResponse createVehicle(VehicleRequest request) {
        String registrationNumber = normalizeRegistrationNumber(request.getRegistrationNumber());
        // Rejects duplicate registration numbers.
        if (vehicleRepository.existsByRegistrationNumber(registrationNumber)) {
            throw new VehicleAlreadyExistsException("Vehicle with this registration number already exists");
        }
        Vehicle vehicle = new Vehicle();
        applyRequest(vehicle, request);
        return toResponse(vehicleRepository.save(vehicle));
    }

    // Updates an existing vehicle.
    @Transactional
    public VehicleResponse updateVehicle(Long id, VehicleRequest request) {
        Vehicle vehicle = findVehicleOrThrow(id);
        String registrationNumber = normalizeRegistrationNumber(request.getRegistrationNumber());
        // Allows the vehicle to keep its own registration number.
        if (vehicleRepository.existsByRegistrationNumberAndIdNot(registrationNumber, id)) {
            throw new VehicleAlreadyExistsException("Vehicle with this registration number already exists");
        }
        applyRequest(vehicle, request);
        return toResponse(vehicleRepository.save(vehicle));
    }

    // Deletes a vehicle if no driver is assigned to it.
    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = findVehicleOrThrow(id);
        if (driverRepository.existsByVehicle_Id(id)) {
            throw new VehicleInUseException("Cannot delete a vehicle assigned to a driver");
        }
        vehicleRepository.delete(vehicle);
    }

    // Finds a vehicle or throws a 404-mapped custom exception.
    private Vehicle findVehicleOrThrow(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with id: " + id));
    }

    // Applies validated request fields to a vehicle.
    private void applyRequest(Vehicle vehicle, VehicleRequest request) {
        vehicle.setRegistrationNumber(normalizeRegistrationNumber(request.getRegistrationNumber()));
        vehicle.setModel(request.getModel().trim());
        vehicle.setVehicleType(request.getVehicleType());
    }

    // Standardizes registration numbers.
    private String normalizeRegistrationNumber(String registrationNumber) {
        return registrationNumber.trim().toUpperCase();
    }

    // Converts a vehicle entity to its response DTO.
    private VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getRegistrationNumber(),
                vehicle.getModel(),
                vehicle.getVehicleType()
        );
    }
}
