package com.preyansh.ridesharing.controller;

import com.preyansh.ridesharing.dto.DriverRequest;
import com.preyansh.ridesharing.dto.DriverResponse;
import com.preyansh.ridesharing.service.DriverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Marks this class as a REST controller.
// Spring converts the returned Java object into a JSON response.
@RestController

// Defines the common URL prefix for this controller's endpoints.
@RequestMapping("/api/drivers")
public class DriverController {

    // The service layer handles business logic and database operations.
    private final DriverService driverService;

    // Constructor injection lets Spring provide the DriverService dependency.
    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    // Handles POST requests to /api/drivers to create a driver.
    @PostMapping
    public ResponseEntity<DriverResponse> createDriver(
            // Converts the incoming JSON request body into a DriverRequest object.
            @RequestBody DriverRequest driverRequest) {

        // Delegate driver creation to the service layer.
        DriverResponse createdDriver = driverService.createDriver(driverRequest);

        // Return HTTP 201 Created with the newly created driver's details.
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDriver);
    }
}