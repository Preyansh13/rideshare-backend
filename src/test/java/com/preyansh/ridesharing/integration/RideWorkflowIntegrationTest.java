package com.preyansh.ridesharing.integration;

import com.preyansh.ridesharing.dto.RideRequest;
import com.preyansh.ridesharing.dto.RideResponse;
import com.preyansh.ridesharing.model.*;
import com.preyansh.ridesharing.repository.*;
import com.preyansh.ridesharing.service.RideOfferService;
import com.preyansh.ridesharing.service.RideService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class RideWorkflowIntegrationTest {

    // Starts a real PostgreSQL container for this test class.
    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine")
                    .withDatabaseName("rideshare_test")
                    .withUsername("test")
                    .withPassword("test");

    // Supplies PostgreSQL container connection details to Spring Boot.
    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private RideService rideService;

    @Autowired
    private RideOfferService rideOfferService;

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
    void shouldAcceptRideOfferAndExpireOtherPendingOffers() {

        // ARRANGE
        // 1. Create and persist a rider.
        User rider = new User();
        rider.setName("Integration Rider");
        rider.setEmail("rider_workflow@test.com");
        rider.setPhone("9000000001");
        rider.setRole(Role.RIDER);

        rider = userRepository.saveAndFlush(rider);

        // 2. Create two driver users.
        User driverUser1 = new User();
        driverUser1.setName("Integration Driver 1");
        driverUser1.setEmail("driver1_workflow@test.com");
        driverUser1.setPhone("9000000002");
        driverUser1.setRole(Role.DRIVER);

        driverUser1 = userRepository.saveAndFlush(driverUser1);

        User driverUser2 = new User();
        driverUser2.setName("Integration Driver 2");
        driverUser2.setEmail("driver2_workflow@test.com");
        driverUser2.setPhone("9000000003");
        driverUser2.setRole(Role.DRIVER);

        driverUser2 = userRepository.saveAndFlush(driverUser2);

        // 3. Create and persist two vehicles.
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setRegistrationNumber("MH12AB1234");
        vehicle1.setModel("Honda City");
        vehicle1.setVehicleType(VehicleType.SEDAN);

        vehicle1 = vehicleRepository.saveAndFlush(vehicle1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setRegistrationNumber("MH12AB5678");
        vehicle2.setModel("Hyundai Creta");
        vehicle2.setVehicleType(VehicleType.SUV);

        vehicle2 = vehicleRepository.saveAndFlush(vehicle2);

        // 4. Create two available drivers.
        Driver driver1 = new Driver();
        driver1.setUser(driverUser1);
        driver1.setVehicle(vehicle1);
        driver1.setLicenseNumber("LIC-WORKFLOW-001");
        driver1.setStatus(DriverStatus.AVAILABLE);

        driver1 = driverRepository.saveAndFlush(driver1);

        Driver driver2 = new Driver();
        driver2.setUser(driverUser2);
        driver2.setVehicle(vehicle2);
        driver2.setLicenseNumber("LIC-WORKFLOW-002");
        driver2.setStatus(DriverStatus.AVAILABLE);

        driver2 = driverRepository.saveAndFlush(driver2);

        // 5. Request a ride through the actual RideService.
        RideRequest request = new RideRequest(
                rider.getId(),
                "Pune Station",
                "Hinjewadi"
        );

        RideResponse rideResponse = rideService.createRide(request);
        Long rideId = rideResponse.id();

        // 6. Create offers for both drivers.
        rideOfferService.createOffer(rideId, driver1.getId());
        rideOfferService.createOffer(rideId, driver2.getId());

        // ACT

        // Driver 1 accepts the ride offer.
        rideOfferService.acceptOffer(rideId, driver1.getId());

        // ASSERT

        // 7. Verify that the ride was assigned to Driver 1.
        Ride savedRide = rideRepository.findById(rideId).orElseThrow();

        assertThat(savedRide.getStatus())
                .isEqualTo(RideStatus.ACCEPTED);

        assertThat(savedRide.getDriver().getId())
                .isEqualTo(driver1.getId());

        // 8. Verify that Driver 1 is now busy.
        Driver savedDriver1 = driverRepository.findById(driver1.getId())
                .orElseThrow();

        assertThat(savedDriver1.getStatus())
                .isEqualTo(DriverStatus.BUSY);

        // 9. Verify that Driver 2 remains available.
        Driver savedDriver2 = driverRepository.findById(driver2.getId())
                .orElseThrow();

        assertThat(savedDriver2.getStatus())
                .isEqualTo(DriverStatus.AVAILABLE);

        // 10. Verify that Driver 1's offer was accepted.
        RideOffer acceptedOffer = rideOfferRepository
                .findByRide_IdAndDriver_Id(rideId, driver1.getId())
                .orElseThrow();

        assertThat(acceptedOffer.getStatus())
                .isEqualTo(RideOfferStatus.ACCEPTED);

        // 11. Verify that Driver 2's pending offer expired.
        RideOffer expiredOffer = rideOfferRepository
                .findByRide_IdAndDriver_Id(rideId, driver2.getId())
                .orElseThrow();

        assertThat(expiredOffer.getStatus())
                .isEqualTo(RideOfferStatus.EXPIRED);
    }

    @Test
    void shouldCompleteRideLifecycleAndMakeDriverAvailableAgain() {

        // ARRANGE: Create a rider.
        User rider = new User();
        rider.setName("Lifecycle Rider");
        rider.setEmail("rider_lifecycle@test.com");
        rider.setPhone("9000000011");
        rider.setRole(Role.RIDER);
        rider = userRepository.saveAndFlush(rider);

        // ARRANGE: Create a driver user.
        User driverUser = new User();
        driverUser.setName("Lifecycle Driver");
        driverUser.setEmail("driver_lifecycle@test.com");
        driverUser.setPhone("9000000012");
        driverUser.setRole(Role.DRIVER);
        driverUser = userRepository.saveAndFlush(driverUser);

        // ARRANGE: Register the driver's vehicle.
        Vehicle vehicle = new Vehicle();
        vehicle.setRegistrationNumber("MH12LC1234");
        vehicle.setModel("Maruti Swift");
        vehicle.setVehicleType(VehicleType.HATCHBACK);
        vehicle = vehicleRepository.saveAndFlush(vehicle);

        // ARRANGE: Create an available driver.
        Driver driver = new Driver();
        driver.setUser(driverUser);
        driver.setVehicle(vehicle);
        driver.setLicenseNumber("LIC-LIFECYCLE-001");
        driver.setStatus(DriverStatus.AVAILABLE);
        driver = driverRepository.saveAndFlush(driver);

        // ACT: Create a ride.
        RideRequest request = new RideRequest(
                rider.getId(),
                "Pune Station",
                "Hinjewadi"
        );

        RideResponse rideResponse = rideService.createRide(request);
        Long rideId = rideResponse.id();

        // ACT: Create and accept the driver's offer.
        rideOfferService.createOffer(rideId, driver.getId());
        rideOfferService.acceptOffer(rideId, driver.getId());

        // Verify the ride has been accepted.
        Ride acceptedRide = rideRepository.findById(rideId).orElseThrow();
        assertThat(acceptedRide.getStatus()).isEqualTo(RideStatus.ACCEPTED);

        // Verify the driver becomes busy after accepting.
        Driver busyDriver = driverRepository.findById(driver.getId()).orElseThrow();
        assertThat(busyDriver.getStatus()).isEqualTo(DriverStatus.BUSY);

        // ACT: Start the ride.
        rideService.startRide(rideId);

        Ride startedRide = rideRepository.findById(rideId).orElseThrow();
        assertThat(startedRide.getStatus()).isEqualTo(RideStatus.IN_PROGRESS);

        // ACT: Complete the ride.
        rideService.completeRide(rideId);

        // ASSERT: Verify the ride is completed.
        Ride completedRide = rideRepository.findById(rideId).orElseThrow();
        assertThat(completedRide.getStatus()).isEqualTo(RideStatus.COMPLETED);

        // ASSERT: Verify the driver becomes available again.
        Driver availableDriver = driverRepository.findById(driver.getId()).orElseThrow();
        assertThat(availableDriver.getStatus()).isEqualTo(DriverStatus.AVAILABLE);
    }
}