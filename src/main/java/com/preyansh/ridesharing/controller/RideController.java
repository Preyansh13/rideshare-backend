package com.preyansh.ridesharing.controller;

import com.preyansh.ridesharing.dto.RideRequest;
import com.preyansh.ridesharing.dto.RideResponse;
import com.preyansh.ridesharing.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Handles HTTP requests related to rides.
@RestController
@RequestMapping("/api/rides")
public class RideController {

    // Provides the business logic for ride creation.
    private final RideService rideService;

    // Constructor injection supplies the ride service.
    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    // Retrieves all rides and returns HTTP 200 OK.
    @GetMapping
    public List<RideResponse> getRides() {

        // Delegate ride retrieval to the service layer.
        return rideService.getRides();
    }

    // Retrieves a ride by its ID and returns HTTP 200 OK.
    @GetMapping("/{id}")
    public ResponseEntity<RideResponse> getRideById(@PathVariable Long id) {

        // Delegate ride retrieval to the service layer.
        RideResponse rideResponse = rideService.getRideById(id);

        // Return the retrieved ride details with a 200 status code.
        return ResponseEntity.ok(rideResponse);
    }

    // Creates a ride from the request and returns HTTP 201 Created.
    @PostMapping
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody RideRequest request) {

        // Delegate ride creation to the service layer.
        RideResponse createdRide = rideService.createRide(request);

        // Return the created ride details with a 201 status code.
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRide);
    }
}
