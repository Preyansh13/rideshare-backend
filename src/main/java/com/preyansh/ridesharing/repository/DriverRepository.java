package com.preyansh.ridesharing.repository;

import com.preyansh.ridesharing.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<Driver, Long> {
}
