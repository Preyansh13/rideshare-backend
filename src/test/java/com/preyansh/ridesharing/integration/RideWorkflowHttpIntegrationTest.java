package com.preyansh.ridesharing.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.preyansh.ridesharing.dto.RideRequest;
import com.preyansh.ridesharing.model.*;
import com.preyansh.ridesharing.repository.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RideWorkflowHttpIntegrationTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine")
                    .withDatabaseName("rideshare_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private RideOfferRepository rideOfferRepository;

    @Test
    void shouldCompleteRideLifecycleThroughHttpRequests() throws Exception {

        // ARRANGE: Create and persist a rider.
        User rider = new User();
        rider.setName("HTTP Integration Rider");
        rider.setEmail("rider_http_workflow@test.com");
        rider.setPhone("9200000001");
        rider.setRole(Role.RIDER);

        rider = userRepository.saveAndFlush(rider);

        // ARRANGE: Create and persist a driver user.
        User driverUser = new User();
        driverUser.setName("HTTP Integration Driver");
        driverUser.setEmail("driver_http_workflow@test.com");
        driverUser.setPhone("9200000002");
        driverUser.setRole(Role.DRIVER);

        driverUser = userRepository.saveAndFlush(driverUser);

        // ARRANGE: Register the driver's vehicle.
        Vehicle vehicle = new Vehicle();
        vehicle.setRegistrationNumber("MH12HTTP1234");
        vehicle.setModel("Honda City");
        vehicle.setVehicleType(VehicleType.SEDAN);

        vehicle = vehicleRepository.saveAndFlush(vehicle);

        // ARRANGE: Create an available driver.
        Driver driver = new Driver();
        driver.setUser(driverUser);
        driver.setVehicle(vehicle);
        driver.setLicenseNumber("LIC-HTTP-WORKFLOW-001");
        driver.setStatus(DriverStatus.AVAILABLE);

        driver = driverRepository.saveAndFlush(driver);

        // ACT: Create a ride through the HTTP endpoint.
        RideRequest rideRequest = new RideRequest(
                rider.getId(),
                "Pune Station",
                "Hinjewadi"
        );

        String createRideResponse = mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rideRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.riderId").value(rider.getId()))
                .andExpect(jsonPath("$.pickupLocation").value("Pune Station"))
                .andExpect(jsonPath("$.dropoffLocation").value("Hinjewadi"))
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode rideJson = objectMapper.readTree(createRideResponse);
        Long rideId = rideJson.get("id").asLong();

        // ACT: Create a ride offer through the HTTP endpoint.
        mockMvc.perform(post("/api/rides/{rideId}/offers/{driverId}",
                        rideId, driver.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rideId").value(rideId))
                .andExpect(jsonPath("$.driverId").value(driver.getId()))
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Verify that the offer was persisted.
        assertThat(rideOfferRepository
                .findByRide_IdAndDriver_Id(rideId, driver.getId()))
                .isPresent();

        // ACT: Accept the ride offer through the HTTP endpoint.
        mockMvc.perform(patch(
                        "/api/rides/{rideId}/offers/{driverId}/accept",
                        rideId, driver.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // Verify that the ride was assigned and the driver became busy.
        Ride acceptedRide = rideRepository.findById(rideId).orElseThrow();

        assertThat(acceptedRide.getStatus()).isEqualTo(RideStatus.ACCEPTED);
        assertThat(acceptedRide.getDriver().getId()).isEqualTo(driver.getId());

        Driver busyDriver = driverRepository.findById(driver.getId()).orElseThrow();

        assertThat(busyDriver.getStatus()).isEqualTo(DriverStatus.BUSY);

        // ACT: Start the ride through the HTTP endpoint.
        mockMvc.perform(patch("/api/rides/{rideId}/start", rideId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // Verify that the ride has started.
        Ride startedRide = rideRepository.findById(rideId).orElseThrow();

        assertThat(startedRide.getStatus()).isEqualTo(RideStatus.IN_PROGRESS);

        // ACT: Complete the ride through the HTTP endpoint.
        mockMvc.perform(patch("/api/rides/{rideId}/complete", rideId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // ASSERT: Verify that the ride is completed.
        Ride completedRide = rideRepository.findById(rideId).orElseThrow();

        assertThat(completedRide.getStatus()).isEqualTo(RideStatus.COMPLETED);
        assertThat(completedRide.getDriver().getId()).isEqualTo(driver.getId());

        // ASSERT: Verify that the driver is available again.
        Driver availableDriver = driverRepository.findById(driver.getId()).orElseThrow();

        assertThat(availableDriver.getStatus()).isEqualTo(DriverStatus.AVAILABLE);

        // ASSERT: Verify that the offer remains accepted.
        RideOffer acceptedOffer = rideOfferRepository
                .findByRide_IdAndDriver_Id(rideId, driver.getId())
                .orElseThrow();

        assertThat(acceptedOffer.getStatus()).isEqualTo(RideOfferStatus.ACCEPTED);
    }
}
