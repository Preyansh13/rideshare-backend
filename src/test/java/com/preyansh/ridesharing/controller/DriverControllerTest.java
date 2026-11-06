package com.preyansh.ridesharing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.preyansh.ridesharing.dto.DriverRequest;
import com.preyansh.ridesharing.dto.DriverResponse;
import com.preyansh.ridesharing.exception.DriverNotFoundException;
import com.preyansh.ridesharing.exception.InvalidDriverStatusException;
import com.preyansh.ridesharing.model.DriverStatus;
import com.preyansh.ridesharing.service.DriverService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DriverController.class)
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DriverService driverService;

    @Test
    void createDriver_shouldReturnCreatedStatus() throws Exception {

        // Prepare the request body.
        DriverRequest request = new DriverRequest();
        request.setUserId(10L);
        request.setLicenseNumber("MH123456789");
        request.setVehicleId(5L);

        // Prepare the mocked service response.
        DriverResponse response = new DriverResponse();
        response.setId(5L);
        response.setUserId(10L);
        response.setLicenseNumber("MH123456789");
        response.setVehicleId(5L);
        response.setStatus(DriverStatus.AVAILABLE);

        when(driverService.createDriver(any(DriverRequest.class)))
                .thenReturn(response);

        // Send POST request and verify the response.
        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.licenseNumber").value("MH123456789"))
                .andExpect(jsonPath("$.vehicleId").value(5))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void getAllDrivers_shouldReturnAllDriversSuccessfully() throws Exception {

        // Prepare two mocked driver responses.
        DriverResponse driver1 = new DriverResponse();
        driver1.setId(1L);
        driver1.setUserId(6L);
        driver1.setLicenseNumber("DL123456");
        driver1.setVehicleId(1L);
        driver1.setStatus(DriverStatus.BUSY);

        DriverResponse driver2 = new DriverResponse();
        driver2.setId(5L);
        driver2.setUserId(10L);
        driver2.setLicenseNumber("MH123456789");
        driver2.setVehicleId(5L);
        driver2.setStatus(DriverStatus.AVAILABLE);

        when(driverService.getAllDrivers())
                .thenReturn(List.of(driver1, driver2));

        // Send GET request and verify both drivers are returned.
        mockMvc.perform(get("/api/drivers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(5));
    }

    @Test
    void getAllDrivers_shouldReturnEmptyList_whenNoDriversExist() throws Exception {

        // Mock the service to return an empty list.
        when(driverService.getAllDrivers()).thenReturn(List.of());

        mockMvc.perform(get("/api/drivers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getDriverById_shouldReturnDriverSuccessfully() throws Exception {

        // Prepare the mocked driver response.
        DriverResponse response = new DriverResponse();
        response.setId(5L);
        response.setUserId(10L);
        response.setLicenseNumber("MH123456789");
        response.setVehicleId(5L);
        response.setStatus(DriverStatus.AVAILABLE);

        when(driverService.getDriverById(5L))
                .thenReturn(response);

        // Send GET request for driver ID 5.
        mockMvc.perform(get("/api/drivers/{driverId}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.vehicleId").value(5))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void getDriverById_shouldReturnNotFound_whenDriverDoesNotExist() throws Exception {

        // Mock the service to throw DriverNotFoundException.
        when(driverService.getDriverById(999L))
                .thenThrow(new DriverNotFoundException("Driver not found"));

        mockMvc.perform(get("/api/drivers/{driverId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateDriver_shouldReturnUpdatedDriverSuccessfully() throws Exception {

        // Prepare the update request.
        DriverRequest request = new DriverRequest();
        request.setUserId(10L);
        request.setLicenseNumber("MH987654321");
        request.setVehicleId(5L);

        // Prepare the updated driver response.
        DriverResponse response = new DriverResponse();
        response.setId(5L);
        response.setUserId(10L);
        response.setLicenseNumber("MH987654321");
        response.setVehicleId(5L);
        response.setStatus(DriverStatus.AVAILABLE);

        when(driverService.updateDriver(eq(5L), any(DriverRequest.class)))
                .thenReturn(response);

        // Send PUT request and verify the updated details.
        mockMvc.perform(put("/api/drivers/{driverId}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.licenseNumber").value("MH987654321"))
                .andExpect(jsonPath("$.vehicleId").value(5));
    }

    @Test
    void updateDriverStatus_shouldReturnUpdatedStatusSuccessfully() throws Exception {

        // Prepare the mocked response with updated BUSY status.
        DriverResponse response = new DriverResponse();
        response.setId(5L);
        response.setUserId(10L);
        response.setLicenseNumber("MH987654321");
        response.setVehicleId(5L);
        response.setStatus(DriverStatus.BUSY);

        when(driverService.updateDriverStatus(5L, DriverStatus.BUSY))
                .thenReturn(response);

        // Send PATCH request with the BUSY status parameter.
        mockMvc.perform(patch("/api/drivers/{driverId}/status", 5L)
                        .param("status", "BUSY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("BUSY"));
    }

    @Test
    void updateDriverStatus_shouldReturnConflict_whenDriverStatusIsInvalid() throws Exception {

        // Mock the service to throw InvalidDriverStatusException.
        when(driverService.updateDriverStatus(5L, DriverStatus.BUSY))
                .thenThrow(new InvalidDriverStatusException("Driver is not available"));

        mockMvc.perform(patch("/api/drivers/{driverId}/status", 5L)
                        .param("status", "BUSY"))
                .andExpect(status().isConflict());
    }

    @Test
    void updateDriverStatus_shouldReturnBadRequest_whenStatusIsInvalid() throws Exception {

        // BUSYYY is not a valid DriverStatus enum value.
        mockMvc.perform(patch("/api/drivers/{driverId}/status", 5L)
                        .param("status", "BUSYYY"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDriverStatus_shouldReturnBadRequest_whenStatusParameterIsMissing() throws Exception {

        // Send PATCH without the required status parameter.
        mockMvc.perform(patch("/api/drivers/{driverId}/status", 5L))
                .andExpect(status().isBadRequest());
    }
}
