package com.preyansh.ridesharing.dto;

import com.preyansh.ridesharing.model.RideStatus;

import java.time.LocalDateTime;

// Defines the ride information returned to the API client.
public record RideResponse(
        Long id,
        Long riderId,
        Long driverId,
        String pickupLocation,
        String dropoffLocation,
        RideStatus status,
        LocalDateTime requestedAt
) {
}
