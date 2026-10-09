package com.preyansh.ridesharing.dto;

import com.preyansh.ridesharing.model.DriverStatus;

// DTO (Data Transfer Object) used to receive driver-related data
// from the client when creating or updating a driver.
public class DriverRequest {

    // ID of the existing user associated with this driver.
    private Long userId;

    // Driver's unique driving license number.
    private String licenseNumber;

    // ID of the vehicle associated with this driver.
    private Long vehicleId;

    // Current status of the driver, represented by the DriverStatus enum.
    private DriverStatus status;

    // No-argument constructor, useful for deserialization of incoming JSON.
    public DriverRequest() {

    }

    // Returns the associated user's ID.
    public Long getUserId() {
        return userId;
    }

    // Sets the associated user's ID.
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // Returns the driver's license number.
    public String getLicenseNumber() {
        return licenseNumber;
    }

    // Sets the driver's license number.
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    // Returns the associated vehicle's ID.
    public Long getVehicleId() {
        return vehicleId;
    }

    // Sets the associated vehicle's ID.
    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    // Returns the driver's current status.
    public DriverStatus getStatus() {
        return status;
    }

    // Sets the driver's status.
    public void setStatus(DriverStatus status) {
        this.status = status;
    }
}