package com.preyansh.ridesharing.model;

import jakarta.persistence.*;

// Marks this class as a JPA entity that maps to a database table.
@Entity

// Specifies the database table name for this entity.
@Table(name = "vehicles")
public class Vehicle {

    // Marks this field as the primary key of the vehicles table.
    @Id

    // Lets the database generate the ID when a new vehicle is inserted.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Stores the vehicle's registration number.
    // unique = true prevents duplicate registration numbers.
    // nullable = false requires the database column to have a value.
    @Column(unique = true, nullable = false)
    private String registrationNumber;

    // Stores the vehicle's model, such as the manufacturer's model name.
    private String model;

    // Stores the vehicle type as a string rather than the enum's numeric ordinal.
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    // No-argument constructor required by JPA when loading entities.
    public Vehicle() {

    }

    // Returns the vehicle's primary key.
    public Long getId() {
        return id;
    }

    // Sets the vehicle's primary key.
    public void setId(Long id) {
        this.id = id;
    }

    // Returns the vehicle's registration number.
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    // Sets the vehicle's registration number.
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    // Returns the vehicle's model.
    public String getModel() {
        return model;
    }

    // Sets the vehicle's model.
    public void setModel(String model) {
        this.model = model;
    }

    // Returns the vehicle's type.
    public VehicleType getVehicleType() {
        return vehicleType;
    }

    // Sets the vehicle's type.
    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }
}