package com.preyansh.ridesharing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.preyansh.ridesharing.dto.VehicleRequest;
import com.preyansh.ridesharing.dto.VehicleResponse;
import com.preyansh.ridesharing.model.VehicleType;
import com.preyansh.ridesharing.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Loads only the VehicleController and the MVC-related components.
@WebMvcTest(VehicleController.class)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Replaces the real VehicleService with a Mockito mock.
    @MockitoBean
    private VehicleService vehicleService;

    // Creates JSON for request bodies and parses JSON responses.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getVehicles_shouldReturnAllVehiclesSuccessfully() throws Exception {

        // Arrange: prepare the vehicle response returned by the service.
        VehicleResponse response = new VehicleResponse(
                1L, "MH12AB1234", "Swift", VehicleType.HATCHBACK
        );

        when(vehicleService.getVehicles()).thenReturn(List.of(response));

        // Act and Assert: call the endpoint and verify the response.
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].registrationNumber").value("MH12AB1234"))
                .andExpect(jsonPath("$[0].model").value("Swift"))
                .andExpect(jsonPath("$[0].vehicleType").value("HATCHBACK"));

        // Verify that the controller called the service.
        verify(vehicleService).getVehicles();
    }

    @Test
    void getVehicleById_shouldReturnVehicleSuccessfully() throws Exception {

        // Arrange: prepare a vehicle response for ID 1.
        VehicleResponse response = new VehicleResponse(
                1L, "MH12AB1234", "Swift", VehicleType.HATCHBACK
        );

        when(vehicleService.getVehicleById(1L)).thenReturn(response);

        // Act and Assert: request the vehicle and verify its details.
        mockMvc.perform(get("/api/vehicles/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.registrationNumber").value("MH12AB1234"))
                .andExpect(jsonPath("$.model").value("Swift"))
                .andExpect(jsonPath("$.vehicleType").value("HATCHBACK"));

        // Verify that the correct ID was passed to the service.
        verify(vehicleService).getVehicleById(1L);
    }

    @Test
    void createVehicle_shouldCreateVehicleSuccessfully() throws Exception {

        // Arrange: prepare a valid vehicle request.
        VehicleRequest request = new VehicleRequest();
        request.setRegistrationNumber("MH12AB1234");
        request.setModel("Swift");
        request.setVehicleType(VehicleType.HATCHBACK);

        // Prepare the response that the service should return.
        VehicleResponse response = new VehicleResponse(
                1L, "MH12AB1234", "Swift", VehicleType.HATCHBACK
        );

        when(vehicleService.createVehicle(any(VehicleRequest.class)))
                .thenReturn(response);

        // Act and Assert: submit the vehicle and verify HTTP 201.
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.registrationNumber").value("MH12AB1234"))
                .andExpect(jsonPath("$.model").value("Swift"))
                .andExpect(jsonPath("$.vehicleType").value("HATCHBACK"));

        // Verify that the service's create method was called.
        verify(vehicleService).createVehicle(any(VehicleRequest.class));
    }

    @Test
    void updateVehicle_shouldUpdateVehicleSuccessfully() throws Exception {

        // Arrange: prepare the updated vehicle details.
        VehicleRequest request = new VehicleRequest();
        request.setRegistrationNumber("MH14CD5678");
        request.setModel("Updated Swift");
        request.setVehicleType(VehicleType.SEDAN);

        // Prepare the updated response returned by the service.
        VehicleResponse response = new VehicleResponse(
                1L, "MH14CD5678", "Updated Swift", VehicleType.SEDAN
        );

        when(vehicleService.updateVehicle(eq(1L), any(VehicleRequest.class)))
                .thenReturn(response);

        // Act and Assert: update vehicle ID 1 and verify the response.
        mockMvc.perform(put("/api/vehicles/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.registrationNumber").value("MH14CD5678"))
                .andExpect(jsonPath("$.model").value("Updated Swift"))
                .andExpect(jsonPath("$.vehicleType").value("SEDAN"));

        // Verify that the correct ID and request were passed to the service.
        verify(vehicleService).updateVehicle(eq(1L), any(VehicleRequest.class));
    }

    @Test
    void deleteVehicle_shouldDeleteVehicleSuccessfully() throws Exception {

        // Arrange: configure the void service method to do nothing.
        doNothing().when(vehicleService).deleteVehicle(1L);

        // Act and Assert: delete vehicle ID 1 and verify HTTP 204.
        mockMvc.perform(delete("/api/vehicles/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // Verify that the service's delete method was called with ID 1.
        verify(vehicleService).deleteVehicle(1L);
    }
}
