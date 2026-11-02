package com.preyansh.ridesharing.controller;

import com.preyansh.ridesharing.dto.DriverRequest;
import com.preyansh.ridesharing.dto.DriverResponse;
import com.preyansh.ridesharing.model.DriverStatus;
import com.preyansh.ridesharing.service.DriverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    // Constructor injection.
    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    // POST /api/drivers
    // Create a new driver.
    @PostMapping
    public ResponseEntity<DriverResponse> createDriver(
            @RequestBody DriverRequest driverRequest) {

        DriverResponse createdDriver = driverService.createDriver(driverRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdDriver);
    }

    // GET /api/drivers
    // Retrieve all drivers.
    @GetMapping
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {

        List<DriverResponse> drivers = driverService.getAllDrivers();

        return ResponseEntity.ok(drivers);
    }

    // GET /api/drivers/{driverId}
    // Retrieve a driver by ID.
    @GetMapping("/{driverId}")
    public ResponseEntity<DriverResponse> getDriverById(
            @PathVariable Long driverId) {

        DriverResponse driver = driverService.getDriverById(driverId);

        return ResponseEntity.ok(driver);
    }

    // PUT /api/drivers/{driverId}
    // Update driver details.
    @PutMapping("/{driverId}")
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable Long driverId,
            @RequestBody DriverRequest driverRequest) {

        DriverResponse updatedDriver =
                driverService.updateDriver(driverId, driverRequest);

        return ResponseEntity.ok(updatedDriver);
    }

    // PATCH /api/drivers/{driverId}/status?status=BUSY
    // Update the driver's operational status.
    @PatchMapping("/{driverId}/status")
    public ResponseEntity<DriverResponse> updateDriverStatus(
            @PathVariable Long driverId,
            @RequestParam DriverStatus status) {

        DriverResponse updatedDriver =
                driverService.updateDriverStatus(driverId, status);

        return ResponseEntity.ok(updatedDriver);
    }
}