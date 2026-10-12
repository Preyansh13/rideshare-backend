package com.preyansh.ridesharing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Contains the information required to request a ride.
public record RideRequest(
        // The ID of the user requesting the ride.
        @NotNull(message = "Rider ID is required")
        @Positive(message = "Rider ID must be positive")
        Long riderId,

        // The location where the ride begins.
        @NotBlank(message = "Pickup location is required")
        String pickupLocation,

        // The destination of the ride.
        @NotBlank(message = "Dropoff location is required")
        String dropoffLocation
) {
}
