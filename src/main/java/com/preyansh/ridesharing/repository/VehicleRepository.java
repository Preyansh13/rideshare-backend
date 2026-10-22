
package com.preyansh.ridesharing.repository;

import com.preyansh.ridesharing.model.Vehicle;

import org.springframework.data.jpa.repository.JpaRepository;

// Repository interface for performing database operations on Vehicle entities.
// Spring Data JPA automatically generates its implementation at runtime.
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    // Checks whether a vehicle with the given registration number already exists.
    // Spring Data JPA derives the database query from the method name.
    // Returns true if a matching vehicle exists; otherwise, returns false.
    boolean existsByRegistrationNumber(String registrationNumber);
    // Checks whether the registration number belongs to another vehicle.
    // "AndIdNot" excludes the vehicle with the specified ID from the search.
    // Useful during updates because a vehicle can retain its own registration number without being incorrectly treated as a duplicate.
    boolean existsByRegistrationNumberAndIdNot(String registrationNumber, Long id);
}
