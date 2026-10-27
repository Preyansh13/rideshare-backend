package com.preyansh.ridesharing.controller;

import com.preyansh.ridesharing.dto.RideResponse;
import com.preyansh.ridesharing.model.RideStatus;
import com.preyansh.ridesharing.service.RideService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(RideController.class)
class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RideService rideService;

    @Test
    void getRideByIdShouldReturnRideAndHttp200() throws Exception {
        // Arrange: Create the expected ride response.
        RideResponse expectedRide = new RideResponse(
                8L,
                2L,
                null,
                "Baner, Pune",
                "Pune Airport",
                RideStatus.REQUESTED,
                LocalDateTime.of(2026, 10, 27, 10, 30)
        );

        // Configure the mock service to return our expected ride.
        when(rideService.getRideById(expectedRide.id()))
                .thenReturn(expectedRide);

        // Act and Assert: Send a GET request and verify the response.
        mockMvc.perform(get("/api/rides/8"))
                .andExpect(status().isOk()) // HTTP status must be 200.
                .andExpect(jsonPath("$.id").value(8)) // Verify ride ID.
                .andExpect(jsonPath("$.pickupLocation").value("Baner, Pune")); // Verify pickup location.
    }
}
