package com.preyansh.ridesharing.integration;

import com.preyansh.ridesharing.model.Ride;
import com.preyansh.ridesharing.model.RideStatus;
import com.preyansh.ridesharing.model.Role;
import com.preyansh.ridesharing.model.User;
import com.preyansh.ridesharing.repository.RideRepository;
import com.preyansh.ridesharing.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RideRepositoryIntegrationTest {

    // Starts a real PostgreSQL container for this test class.
    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine")
                    .withDatabaseName("rideshare_test")
                    .withUsername("test")
                    .withPassword("test");

    // Supplies the container's connection details to Spring Boot.
    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserRepository userRepository;

    // Allows us to clear the JPA persistence context.
    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldSaveAndRetrieveRideWithRider() {

        // Arrange: create a rider.
        User rider = new User();
        rider.setName("Integration Test Rider");
        rider.setEmail("integration@test.com");
        rider.setPhone("9876543210");
        rider.setRole(Role.RIDER);

        // Persist the rider first because Ride does not cascade persistence.
        User savedRider = userRepository.saveAndFlush(rider);

        // Create a ride associated with the persisted rider.
        Ride ride = new Ride();
        ride.setRider(savedRider);
        ride.setPickupLocation("Pune Station");
        ride.setDropoffLocation("Hinjewadi");
        ride.setStatus(RideStatus.REQUESTED);

        // Persist the ride in PostgreSQL.
        Ride savedRide = rideRepository.saveAndFlush(ride);

        // Clear the persistence context to force a database retrieval.
        entityManager.clear();

        // Retrieve the ride using its generated ID.
        Ride retrievedRide = rideRepository.findById(savedRide.getId())
                .orElseThrow();

        // Assert: verify the ride was correctly persisted.
        assertThat(retrievedRide.getId()).isNotNull();
        assertThat(retrievedRide.getPickupLocation())
                .isEqualTo("Pune Station");
        assertThat(retrievedRide.getDropoffLocation())
                .isEqualTo("Hinjewadi");
        assertThat(retrievedRide.getStatus())
                .isEqualTo(RideStatus.REQUESTED);

        // Verify the associated rider was persisted correctly.
        assertThat(retrievedRide.getRider().getEmail())
                .isEqualTo("integration@test.com");

        // Verify the @PrePersist callback populated the timestamp.
        assertThat(retrievedRide.getRequestedAt()).isNotNull();
    }
}
