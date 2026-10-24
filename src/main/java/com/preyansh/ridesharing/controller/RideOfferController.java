package com.preyansh.ridesharing.controller;

import com.preyansh.ridesharing.dto.RideOfferResponse;
import com.preyansh.ridesharing.service.RideOfferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rides/{rideId}/offers")
public class RideOfferController {

    private final RideOfferService rideOfferService;

    // Constructor injection for the offer service.
    public RideOfferController(RideOfferService rideOfferService) {
        this.rideOfferService = rideOfferService;
    }

    // Create an offer for a particular driver.
    @PostMapping("/{driverId}")
    public ResponseEntity<RideOfferResponse> createOffer(
            @PathVariable Long rideId,
            @PathVariable Long driverId) {
        RideOfferResponse response = rideOfferService.createOffer(rideId, driverId);
        // 201 Created indicates that a new offer was created.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Let the driver accept an existing pending offer.
    @PatchMapping("/{driverId}/accept")
    public ResponseEntity<RideOfferResponse> acceptOffer(
            @PathVariable Long rideId,
            @PathVariable Long driverId) {
        RideOfferResponse response = rideOfferService.acceptOffer(rideId, driverId);
        return ResponseEntity.ok(response);
    }

    // Let the driver reject an existing pending offer.
    @PatchMapping("/{driverId}/reject")
    public ResponseEntity<RideOfferResponse> rejectOffer(
            @PathVariable Long rideId,
            @PathVariable Long driverId) {
        RideOfferResponse response = rideOfferService.rejectOffer(rideId, driverId);
        return ResponseEntity.ok(response);
    }
}