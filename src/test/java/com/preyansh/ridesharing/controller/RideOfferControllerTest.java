package com.preyansh.ridesharing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.preyansh.ridesharing.dto.RideOfferResponse;
import com.preyansh.ridesharing.model.RideOfferStatus;
import com.preyansh.ridesharing.service.RideOfferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RideOfferController.class)
class RideOfferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock the service so these tests focus only on controller behaviour.
    @MockitoBean
    private RideOfferService rideOfferService;

    // Create an ObjectMapper for JSON response handling if needed.
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Test POST /api/rides/{rideId}/offers/{driverId}.
    @Test
    void createOffer_shouldCreateOfferAndReturnHttp201() throws Exception {

        Long rideId = 1L;
        Long driverId = 2L;

        // Prepare the response returned by the mocked service.
        RideOfferResponse response = new RideOfferResponse(
                10L,
                rideId,
                driverId,
                RideOfferStatus.PENDING,
                LocalDateTime.of(2026, 10, 27, 10, 30)
        );

        // Tell Mockito what the service should return.
        when(rideOfferService.createOffer(rideId, driverId))
                .thenReturn(response);

        // Send the POST request and verify the HTTP response.
        mockMvc.perform(post("/api/rides/{rideId}/offers/{driverId}", rideId, driverId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.rideId").value(rideId))
                .andExpect(jsonPath("$.driverId").value(driverId))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.createdAt").exists());

        // Verify that the controller called the service with the correct IDs.
        verify(rideOfferService).createOffer(rideId, driverId);
    }

    // Test PATCH /api/rides/{rideId}/offers/{driverId}/accept.
    @Test
    void acceptOffer_shouldAcceptOfferAndReturnHttp200() throws Exception {

        Long rideId = 1L;
        Long driverId = 2L;

        // Prepare an ACCEPTED offer response.
        RideOfferResponse response = new RideOfferResponse(
                10L,
                rideId,
                driverId,
                RideOfferStatus.ACCEPTED,
                LocalDateTime.of(2026, 10, 27, 10, 30)
        );

        // Mock the service response.
        when(rideOfferService.acceptOffer(rideId, driverId))
                .thenReturn(response);

        // Send the PATCH request and verify its response.
        mockMvc.perform(patch("/api/rides/{rideId}/offers/{driverId}/accept", rideId, driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.rideId").value(rideId))
                .andExpect(jsonPath("$.driverId").value(driverId))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.createdAt").exists());

        // Verify that the correct service method was called.
        verify(rideOfferService).acceptOffer(rideId, driverId);
    }

    // Test PATCH /api/rides/{rideId}/offers/{driverId}/reject.
    @Test
    void rejectOffer_shouldRejectOfferAndReturnHttp200() throws Exception {

        Long rideId = 1L;
        Long driverId = 2L;

        // Prepare a REJECTED offer response.
        RideOfferResponse response = new RideOfferResponse(
                10L,
                rideId,
                driverId,
                RideOfferStatus.REJECTED,
                LocalDateTime.of(2026, 10, 27, 10, 30)
        );

        // Mock the service response.
        when(rideOfferService.rejectOffer(rideId, driverId))
                .thenReturn(response);

        // Send the PATCH request and verify its response.
        mockMvc.perform(patch("/api/rides/{rideId}/offers/{driverId}/reject", rideId, driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.rideId").value(rideId))
                .andExpect(jsonPath("$.driverId").value(driverId))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.createdAt").exists());

        // Verify that the correct service method was called.
        verify(rideOfferService).rejectOffer(rideId, driverId);
    }
}
