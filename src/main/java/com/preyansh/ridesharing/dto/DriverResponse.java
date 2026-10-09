package com.preyansh.ridesharing.dto;

import com.preyansh.ridesharing.model.DriverStatus;

// DTO (Data Transfer Object) used to send driver information
// from the application to the client in API responses.
public class DriverResponse {

    // Unique ID assigned to the driver.
    private Long id;

    // ID of the user associated with this driver.
    private Long userId;

    // Driver's driving license number.
    private String licenseNumber;

    // ID of the vehicle associated with this driver.
    private Long vehicleId;

    // Current status of the driver, represented by the DriverStatus enum.
    private DriverStatus status;

    // No-argument constructor, useful when creating an empty response object.
    public DriverResponse() {

    }

    // Returns the driver's unique ID.
    public Long getId() {
        return id;
    }

    // Sets the driver's unique ID.
    public void setId(Long id) {
        this.id = id;
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

    // Sets the driver's current status.
    public void setStatus(DriverStatus status) {
        this.status = status;
    }
}