package com.preyansh.ridesharing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.preyansh.ridesharing.dto.UserRequest;
import com.preyansh.ridesharing.dto.UserResponse;
import com.preyansh.ridesharing.service.UserService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    // ObjectMapper converts request objects into JSON.
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Creates a sample UserResponse for mocked service responses.
    private UserResponse createUserResponse() {

        UserResponse response = new UserResponse();

        response.setId(1L);
        response.setName("Preyansh");
        response.setEmail("preyansh@example.com");
        response.setPhone("9876543210");

        return response;
    }

    @Test
    void getUsers_shouldReturnPaginatedUsersSuccessfully() throws Exception {

        // Prepare a sample user response.
        UserResponse user = createUserResponse();

        // Create a page containing the sample user.
        Page<UserResponse> userPage = new PageImpl<>(
                List.of(user),
                PageRequest.of(0, 10),
                1
        );

        // Mock the service response.
        when(userService.getUsers(any())).thenReturn(userPage);

        // Send a GET request with pagination parameters.
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                // Verify the HTTP response.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Preyansh"))
                .andExpect(jsonPath("$.content[0].email")
                        .value("preyansh@example.com"))
                .andExpect(jsonPath("$.content[0].phone")
                        .value("9876543210"));

        // Verify that the service method was called.
        verify(userService).getUsers(any());
    }

    @Test
    void getUserById_shouldReturnUserSuccessfully() throws Exception {

        // Prepare a sample response.
        UserResponse user = createUserResponse();

        // Mock the service response.
        when(userService.getUserById(1L)).thenReturn(user);

        // Send a GET request for the specified user.
        mockMvc.perform(get("/api/users/1"))
                // Verify the HTTP status and response body.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Preyansh"))
                .andExpect(jsonPath("$.email")
                        .value("preyansh@example.com"))
                .andExpect(jsonPath("$.phone")
                        .value("9876543210"));

        verify(userService).getUserById(1L);
    }

    @Test
    void createUser_shouldCreateUserSuccessfully() throws Exception {

        // Prepare a valid JSON request.
        String requestJson = objectMapper.writeValueAsString(Map.of(
                "name", "Preyansh",
                "email", "preyansh@example.com",
                "phone", "9876543210"
        ));

        // Mock the service response.
        when(userService.createUser(any(UserRequest.class)))
                .thenReturn(createUserResponse());

        // Send a POST request with the user details.
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(requestJson))
                // Verify HTTP 201 Created and response data.
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Preyansh"))
                .andExpect(jsonPath("$.email")
                        .value("preyansh@example.com"))
                .andExpect(jsonPath("$.phone")
                        .value("9876543210"));

        verify(userService).createUser(any(UserRequest.class));
    }

    @Test
    void createUser_shouldReturnBadRequestWhenRequestIsInvalid()
            throws Exception {

        // Prepare a request containing invalid user details.
        String requestJson = objectMapper.writeValueAsString(Map.of(
                "name", "",
                "email", "invalid-email",
                "phone", "123"
        ));

        // Send a POST request with invalid data.
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(requestJson))
                // @Valid should reject the invalid request.
                .andExpect(status().isBadRequest());

        // Service should not be called for invalid input.
        verify(userService, never()).createUser(any(UserRequest.class));
    }

    @Test
    void updateUser_shouldUpdateUserSuccessfully() throws Exception {

        // Prepare a valid update request.
        String requestJson = objectMapper.writeValueAsString(Map.of(
                "name", "Preyansh Sharma",
                "email", "preyansh@example.com",
                "phone", "9876543210"
        ));

        // Prepare the updated response.
        UserResponse updatedUser = createUserResponse();
        updatedUser.setName("Preyansh Sharma");

        // Mock the service response.
        when(userService.updateUser(eq(1L), any(UserRequest.class)))
                .thenReturn(updatedUser);

        // Send a PUT request for the specified user.
        mockMvc.perform(put("/api/users/1")
                        .contentType("application/json")
                        .content(requestJson))
                // Verify the HTTP response.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Preyansh Sharma"));

        verify(userService).updateUser(eq(1L), any(UserRequest.class));
    }

    @Test
    void updateUser_shouldReturnBadRequestWhenRequestIsInvalid()
            throws Exception {

        // Prepare an invalid update request.
        String requestJson = objectMapper.writeValueAsString(Map.of(
                "name", "",
                "email", "invalid-email",
                "phone", "123"
        ));

        // Send a PUT request with invalid data.
        mockMvc.perform(put("/api/users/1")
                        .contentType("application/json")
                        .content(requestJson))
                // Verify that validation rejects the request.
                .andExpect(status().isBadRequest());

        verify(userService, never())
                .updateUser(eq(1L), any(UserRequest.class));
    }

    @Test
    void deleteUser_shouldDeleteUserSuccessfully() throws Exception {

        // Mock the void service method.
        doNothing().when(userService).deleteUser(1L);

        // Send a DELETE request.
        mockMvc.perform(delete("/api/users/1"))
                // Verify HTTP 204 No Content.
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(userService).deleteUser(1L);
    }

    @Test
    void patchUser_shouldPartiallyUpdateUserSuccessfully() throws Exception {

        // Prepare a request containing only the name field.
        String requestJson = objectMapper.writeValueAsString(Map.of(
                "name", "Preyansh Sharma"
        ));

        // Prepare the updated response.
        UserResponse updatedUser = createUserResponse();
        updatedUser.setName("Preyansh Sharma");

        // Mock the service response.
        when(userService.patchUser(eq(1L), any()))
                .thenReturn(updatedUser);

        // Send a PATCH request.
        mockMvc.perform(patch("/api/users/1")
                        .contentType("application/json")
                        .content(requestJson))
                // Verify the response.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Preyansh Sharma"));

        verify(userService).patchUser(eq(1L), any());
    }

    @Test
    void searchUsersByName_shouldReturnMatchingUsersSuccessfully()
            throws Exception {

        // Prepare the matching user response.
        UserResponse user = createUserResponse();

        // Mock the search result.
        when(userService.searchUsersByName("Preyansh"))
                .thenReturn(List.of(user));

        // Send a GET request with the name parameter.
        mockMvc.perform(get("/api/users/search/name")
                        .param("name", "Preyansh"))
                // Verify the matching user is returned.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Preyansh"));

        verify(userService).searchUsersByName("Preyansh");
    }

    @Test
    void searchUsersByName_shouldReturnBadRequestWhenNameIsMissing()
            throws Exception {

        // Send a search request without the required name parameter.
        mockMvc.perform(get("/api/users/search/name"))
                // Controller rejects a missing search parameter.
                .andExpect(status().isBadRequest());

        verify(userService, never()).searchUsersByName(anyString());
    }

    @Test
    void searchUsersByName_shouldReturnBadRequestWhenNameIsBlank()
            throws Exception {

        // Send a search request with a blank name.
        mockMvc.perform(get("/api/users/search/name")
                        .param("name", "   "))
                // Controller rejects a blank search parameter.
                .andExpect(status().isBadRequest());

        verify(userService, never()).searchUsersByName(anyString());
    }

    @Test
    void searchUsersByEmail_shouldReturnMatchingUsersSuccessfully()
            throws Exception {

        // Prepare the matching user response.
        UserResponse user = createUserResponse();

        // Mock the search result.
        when(userService.searchUsersByEmail("preyansh@example.com"))
                .thenReturn(List.of(user));

        // Send a GET request with the email parameter.
        mockMvc.perform(get("/api/users/search/email")
                        .param("email", "preyansh@example.com"))
                // Verify the matching user is returned.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email")
                        .value("preyansh@example.com"));

        verify(userService).searchUsersByEmail("preyansh@example.com");
    }

    @Test
    void searchUsersByEmail_shouldReturnBadRequestWhenEmailIsMissing()
            throws Exception {

        // Send a search request without the email parameter.
        mockMvc.perform(get("/api/users/search/email"))
                // Controller rejects a missing search parameter.
                .andExpect(status().isBadRequest());

        verify(userService, never()).searchUsersByEmail(anyString());
    }

    @Test
    void searchUsersByEmail_shouldReturnBadRequestWhenEmailIsBlank()
            throws Exception {

        // Send a search request with a blank email.
        mockMvc.perform(get("/api/users/search/email")
                        .param("email", "   "))
                // Controller rejects a blank search parameter.
                .andExpect(status().isBadRequest());

        verify(userService, never()).searchUsersByEmail(anyString());
    }
}
