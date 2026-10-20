package com.preyansh.ridesharing.controller;

import com.preyansh.ridesharing.dto.RideRequest;
import com.preyansh.ridesharing.dto.RideResponse;
import com.preyansh.ridesharing.model.RideStatus;
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

    // Retrieves all rides belonging to a specific rider.
    @GetMapping("/rider/{riderId}")
    public List<RideResponse> getRidesByRiderId(@PathVariable Long riderId) {
        // Delegate ride retrieval to the service layer.
        return rideService.getRidesByRiderId(riderId);
    }

    // Retrieves all rides assigned to a specific driver.
    @GetMapping("/driver/{driverId}")
    public List<RideResponse> getRidesByDriverId(@PathVariable Long driverId) {
        // Delegate driver-based ride retrieval to the service layer.
        return rideService.getRidesByDriverId(driverId);
    }

    // Retrieves rides matching the specified status.
    @GetMapping("/status/{status}")
    public List<RideResponse> getRidesByStatus(@PathVariable RideStatus status) {
        // Delegate status-based retrieval to the service layer.
        return rideService.getRidesByStatus(status);
    }

    // Creates a ride from the request and returns HTTP 201 Created.
    @PostMapping
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody RideRequest request) {
        // Delegate ride creation to the service layer.
        RideResponse createdRide = rideService.createRide(request);
        // Return the created ride details with a 201 status code.
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRide);
    }

    // Cancels a ride by its ID and returns HTTP 200 OK.
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RideResponse> cancelRide(@PathVariable Long id) {
        // Delegate ride cancellation and status validation to the service layer.
        RideResponse cancelledRide = rideService.cancelRide(id);
        // Return the updated ride details with a 200 status code.
        return ResponseEntity.ok(cancelledRide);
    }

    // Accepts a ride for a specific driver.
    // PATCH is used because this operation updates the existing ride's state.
    @PatchMapping("/{rideId}/accept/{driverId}")
    public ResponseEntity<RideResponse> acceptRide(@PathVariable Long rideId, @PathVariable Long driverId) {
        // Calls the service layer to validate the ride and driver,
        // assign the driver to the ride, and update their statuses.
        RideResponse acceptedRide = rideService.acceptRide(rideId, driverId);
        // Returns HTTP 200 OK along with the updated ride details.
        return ResponseEntity.ok(acceptedRide);
    }

    // Rejects a ride request for a specific driver.
    @PatchMapping("/{rideId}/reject/{driverId}")
    public ResponseEntity<RideResponse> rejectRide(@PathVariable Long rideId, @PathVariable Long driverId) {
        // Calls the service layer to validate and reject the ride.
        RideResponse rejectedRide = rideService.rejectRide(rideId, driverId);
        // Returns HTTP 200 OK with the updated ride details.
        return ResponseEntity.ok(rejectedRide);
    }

    //Starts a ride
    @PatchMapping("/{rideId}/start")
    public ResponseEntity<RideResponse> startRide(@PathVariable Long rideId) {
        // Call the service layer to validate the ride's current status and update its status from ACCEPTED to IN_PROGRESS.
        RideResponse startedRide = rideService.startRide(rideId);
        // Return HTTP 200 OK with the updated RideResponse in the body.
        return ResponseEntity.ok(startedRide);
    }
}
