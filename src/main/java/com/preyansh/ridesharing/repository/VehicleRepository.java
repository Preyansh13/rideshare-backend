package com.preyansh.ridesharing.repository;

import com.preyansh.ridesharing.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}
