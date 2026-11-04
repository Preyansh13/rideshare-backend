package com.preyansh.ridesharing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.preyansh.ridesharing.dto.RideRequest;
import com.preyansh.ridesharing.dto.RideResponse;
import com.preyansh.ridesharing.exception.RideNotFoundException;
import com.preyansh.ridesharing.model.RideStatus;
import com.preyansh.ridesharing.service.RideService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RideController.class)
class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Replace the real RideService with a Mockito mock.
    @MockitoBean
    private RideService rideService;

    // Converts Java request objects into JSON.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getRides_shouldReturnAllRidesAndHttp200() throws Exception {

        // Arrange: Create sample ride responses.
        RideResponse ride1 = createRideResponse(8L, 2L, null, RideStatus.REQUESTED);
        RideResponse ride2 = createRideResponse(9L, 3L, 1L, RideStatus.ACCEPTED);

        // Configure the mock service to return both rides.
        when(rideService.getRides()).thenReturn(List.of(ride1, ride2));

        // Act and Assert: Send GET request and verify the response.
        mockMvc.perform(get("/api/rides"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(8))
                .andExpect(jsonPath("$[0].status").value("REQUESTED"))
                .andExpect(jsonPath("$[1].id").value(9))
                .andExpect(jsonPath("$[1].status").value("ACCEPTED"));

        // Verify that the controller called the service.
        verify(rideService).getRides();
    }

    @Test
    void getRideById_shouldReturnRideAndHttp200() throws Exception {

        // Arrange: Create the expected ride response.
        RideResponse expectedRide = createRideResponse(
                8L, 2L, null, RideStatus.REQUESTED
        );

        // Configure the mock service to return our expected ride.
        when(rideService.getRideById(8L)).thenReturn(expectedRide);

        // Act and Assert: Send a GET request and verify the response.
        mockMvc.perform(get("/api/rides/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.pickupLocation").value("Baner, Pune"))
                .andExpect(jsonPath("$.dropoffLocation").value("Pune Airport"))
                .andExpect(jsonPath("$.status").value("REQUESTED"));

        // Verify that the correct ID was passed to the service.
        verify(rideService).getRideById(8L);
    }

    @Test
    void getRideById_shouldReturn404WhenRideDoesNotExist() throws Exception {

        // Arrange: Configure the service to throw an exception for ride 999.
        when(rideService.getRideById(999L))
                .thenThrow(new RideNotFoundException("Ride not found with id: 999"));

        // Act and Assert: Request a ride that does not exist.
        mockMvc.perform(get("/api/rides/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Ride not found with id: 999"))
                .andExpect(jsonPath("$.path").value("/api/rides/999"));

        // Verify that the service was called with ID 999.
        verify(rideService).getRideById(999L);
    }

    @Test
    void getRidesByRiderId_shouldReturnRiderRidesAndHttp200() throws Exception {

        // Arrange: Prepare rides belonging to rider ID 2.
        List<RideResponse> rides = List.of(
                createRideResponse(8L, 2L, null, RideStatus.REQUESTED),
                createRideResponse(9L, 2L, 1L, RideStatus.ACCEPTED)
        );

        when(rideService.getRidesByRiderId(2L)).thenReturn(rides);

        // Act and Assert: Retrieve rides for rider ID 2.
        mockMvc.perform(get("/api/rides/rider/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].riderId").value(2))
                .andExpect(jsonPath("$[1].riderId").value(2));

        // Verify that the correct rider ID was passed to the service.
        verify(rideService).getRidesByRiderId(2L);
    }

    @Test
    void getRidesByDriverId_shouldReturnDriverRidesAndHttp200() throws Exception {

        // Arrange: Prepare rides assigned to driver ID 1.
        List<RideResponse> rides = List.of(
                createRideResponse(9L, 2L, 1L, RideStatus.ACCEPTED)
        );

        when(rideService.getRidesByDriverId(1L)).thenReturn(rides);

        // Act and Assert: Retrieve rides assigned to driver ID 1.
        mockMvc.perform(get("/api/rides/driver/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].driverId").value(1));

        // Verify that the correct driver ID was passed to the service.
        verify(rideService).getRidesByDriverId(1L);
    }

    @Test
    void getRidesByStatus_shouldReturnMatchingRidesAndHttp200() throws Exception {

        // Arrange: Prepare rides having REQUESTED status.
        List<RideResponse> rides = List.of(
                createRideResponse(8L, 2L, null, RideStatus.REQUESTED)
        );

        when(rideService.getRidesByStatus(RideStatus.REQUESTED))
                .thenReturn(rides);

        // Act and Assert: Retrieve rides using the REQUESTED status.
        mockMvc.perform(get("/api/rides/status/REQUESTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("REQUESTED"));

        // Verify that the correct status was passed to the service.
        verify(rideService).getRidesByStatus(RideStatus.REQUESTED);
    }

    @Test
    void createRide_shouldCreateRideAndReturnHttp201() throws Exception {

        // Arrange: Create a valid ride request.
        RideRequest request = new RideRequest(
                2L,
                "Baner, Pune",
                "Pune Airport"
        );

        // Prepare the response returned by the mocked service.
        RideResponse createdRide = createRideResponse(
                8L, 2L, null, RideStatus.REQUESTED
        );

        when(rideService.createRide(any(RideRequest.class)))
                .thenReturn(createdRide);

        // Act and Assert: Submit the ride request.
        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.riderId").value(2))
                .andExpect(jsonPath("$.pickupLocation").value("Baner, Pune"))
                .andExpect(jsonPath("$.dropoffLocation").value("Pune Airport"))
                .andExpect(jsonPath("$.status").value("REQUESTED"));

        // Verify that the service's create method was called.
        verify(rideService).createRide(any(RideRequest.class));
    }

    @Test
    void cancelRide_shouldCancelRideAndReturnHttp200() throws Exception {

        // Arrange: Prepare the ride response after cancellation.
        RideResponse cancelledRide = createRideResponse(
                8L, 2L, null, RideStatus.CANCELLED
        );

        when(rideService.cancelRide(8L)).thenReturn(cancelledRide);

        // Act and Assert: Send a PATCH request to cancel the ride.
        mockMvc.perform(patch("/api/rides/8/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Verify that the service was called with the correct ride ID.
        verify(rideService).cancelRide(8L);
    }

    @Test
    void startRide_shouldStartRideAndReturnHttp200() throws Exception {

        // Arrange: Prepare the ride response after starting the ride.
        RideResponse startedRide = createRideResponse(
                8L, 2L, 1L, RideStatus.IN_PROGRESS
        );

        when(rideService.startRide(8L)).thenReturn(startedRide);

        // Act and Assert: Send a PATCH request to start the ride.
        mockMvc.perform(patch("/api/rides/8/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.driverId").value(1))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // Verify that the service was called with the correct ride ID.
        verify(rideService).startRide(8L);
    }

    @Test
    void completeRide_shouldCompleteRideAndReturnHttp200() throws Exception {

        // Arrange: Prepare the ride response after completion.
        RideResponse completedRide = createRideResponse(
                8L, 2L, 1L, RideStatus.COMPLETED
        );

        when(rideService.completeRide(8L)).thenReturn(completedRide);

        // Act and Assert: Send a PATCH request to complete the ride.
        mockMvc.perform(patch("/api/rides/8/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.driverId").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Verify that the service was called with the correct ride ID.
        verify(rideService).completeRide(8L);
    }

    // Helper method to create consistent RideResponse objects for tests.
    private RideResponse createRideResponse(
            Long id,
            Long riderId,
            Long driverId,
            RideStatus status
    ) {
        return new RideResponse(
                id,
                riderId,
                driverId,
                "Baner, Pune",
                "Pune Airport",
                status,
                LocalDateTime.of(2026, 10, 27, 10, 30)
        );
    }
}
