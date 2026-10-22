package com.preyansh.ridesharing.dto;

import com.preyansh.ridesharing.model.VehicleType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// DTO (Data Transfer Object) that receives vehicle data from API clients.
// It separates incoming API data from the Vehicle database entity.
public class VehicleRequest {
    // Registration number must contain at least one non-whitespace character.
    // If validation fails, Spring uses this message in the validation error.
    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    // Vehicle model must not be null, empty, or only whitespace.
    @NotBlank(message = "Vehicle model is required")
    private String model;

    // Vehicle type is required.
    // @NotNull is used because VehicleType is an enum, not a String.
    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    // Returns the registration number provided in the request.
    // Spring/Jackson can use this getter when accessing the DTO property.
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    // Sets the registration number when the incoming JSON is converted to this DTO.
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    // Returns the vehicle model.
    public String getModel() {
        return model;
    }

    // Sets the vehicle model from the incoming request.
    public void setModel(String model) {
        this.model = model;
    }

    // Returns the selected vehicle type.
    public VehicleType getVehicleType() {
        return vehicleType;
    }

    // Sets the vehicle type from the incoming request.
    // The JSON value must match an enum constant, such as "HATCHBACK".
    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }
}