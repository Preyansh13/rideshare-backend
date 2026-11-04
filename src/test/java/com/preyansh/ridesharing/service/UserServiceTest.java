package com.preyansh.ridesharing.service;

import com.preyansh.ridesharing.dto.UserPatchRequest;
import com.preyansh.ridesharing.dto.UserRequest;
import com.preyansh.ridesharing.dto.UserResponse;
import com.preyansh.ridesharing.exception.UserAlreadyExistsException;
import com.preyansh.ridesharing.exception.UserNotFoundException;
import com.preyansh.ridesharing.model.Role;
import com.preyansh.ridesharing.model.User;
import com.preyansh.ridesharing.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // Test retrieving a page containing users.
    @Test
    void getUsers_shouldReturnPaginatedUsersSuccessfully() {

        Pageable pageable = PageRequest.of(0, 10);

        // Prepare sample users returned by the repository.
        User user1 = createUser(1L, "Preyansh", "preyansh@gmail.com",
                "9876543210", Role.RIDER);

        User user2 = createUser(2L, "Rahul", "rahul@gmail.com",
                "9876543211", Role.DRIVER);

        Page<User> userPage = new PageImpl<>(
                List.of(user1, user2),
                pageable,
                2
        );

        // Mock the repository's paginated query.
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // Call the service method.
        Page<UserResponse> result = userService.getUsers(pageable);

        // Verify pagination details and returned user information.
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("Preyansh", result.getContent().get(0).getName());
        assertEquals("Rahul", result.getContent().get(1).getName());

        // Verify that the repository was called with the supplied pageable.
        verify(userRepository).findAll(pageable);
    }

    // Test retrieving users when the repository returns an empty page.
    @Test
    void getUsers_shouldReturnEmptyPageWhenNoUsersExist() {

        Pageable pageable = PageRequest.of(0, 10);

        // Prepare an empty page.
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        // Mock the repository response.
        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        // Call the service method.
        Page<UserResponse> result = userService.getUsers(pageable);

        // Verify that the returned page contains no users.
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        // Verify the repository interaction.
        verify(userRepository).findAll(pageable);
    }

    // Test retrieving a user when the supplied ID exists.
    @Test
    void getUserById_shouldReturnUserSuccessfully() {

        Long userId = 1L;

        User user = createUser(
                userId,
                "Preyansh",
                "preyansh@gmail.com",
                "9876543210",
                Role.RIDER
        );

        // Mock the repository to return the requested user.
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Call the service method.
        UserResponse result = userService.getUserById(userId);

        // Verify that the entity was converted into the correct response DTO.
        assertEquals(userId, result.getId());
        assertEquals("Preyansh", result.getName());
        assertEquals("preyansh@gmail.com", result.getEmail());
        assertEquals("9876543210", result.getPhone());
        assertEquals(Role.RIDER, result.getRole());

        verify(userRepository).findById(userId);
    }

    // Test retrieving a user when the supplied ID does not exist.
    @Test
    void getUserById_shouldThrowExceptionWhenUserDoesNotExist() {

        Long userId = 999L;

        // Mock the repository to indicate that the user was not found.
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Verify that the service throws UserNotFoundException.
        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(userId)
        );

        // Verify that the repository was queried using the supplied ID.
        verify(userRepository).findById(userId);
    }

    // Test creating a user with valid, unique details.
    @Test
    void createUser_shouldCreateUserSuccessfully() {

        UserRequest request = createUserRequest(
                "Preyansh",
                "preyansh@gmail.com",
                "9876543210"
        );

        // No existing user should have this email or phone number.
        when(userRepository.findByEmail("preyansh@gmail.com"))
                .thenReturn(Optional.empty());

        when(userRepository.existsByPhone("9876543210"))
                .thenReturn(false);

        // Simulate saving the entity and database-generated ID.
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        // Call the service method.
        UserResponse result = userService.createUser(request);

        // Verify the returned response.
        assertEquals(1L, result.getId());
        assertEquals("Preyansh", result.getName());
        assertEquals("preyansh@gmail.com", result.getEmail());
        assertEquals("9876543210", result.getPhone());

        // Newly created users are assigned the RIDER role by default.
        assertEquals(Role.RIDER, result.getRole());

        // Verify that the entity was saved.
        verify(userRepository).save(any(User.class));
    }

    // Test that email normalization happens before checking uniqueness.
    @Test
    void createUser_shouldNormalizeEmailBeforeCheckingDuplicate() {

        UserRequest request = createUserRequest(
                "Preyansh",
                "  PREYANSH@GMAIL.COM  ",
                "9876543210"
        );

        // The normalized email should be used for the repository lookup.
        when(userRepository.findByEmail("preyansh@gmail.com"))
                .thenReturn(Optional.empty());

        when(userRepository.existsByPhone("9876543210"))
                .thenReturn(false);

        // Simulate saving the normalized user.
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        // Call the service method.
        UserResponse result = userService.createUser(request);

        // Verify that whitespace was removed and email converted to lowercase.
        assertEquals("preyansh@gmail.com", result.getEmail());

        // Verify that the normalized email was used for the uniqueness check.
        verify(userRepository).findByEmail("preyansh@gmail.com");
    }

    // Test that creation fails when the email already exists.
    @Test
    void createUser_shouldThrowExceptionWhenEmailAlreadyExists() {

        UserRequest request = createUserRequest(
                "Preyansh",
                "preyansh@gmail.com",
                "9876543210"
        );

        User existingUser = createUser(
                2L,
                "Rahul",
                "preyansh@gmail.com",
                "9876543211",
                Role.RIDER
        );

        // Mock the repository to return an existing user with the same email.
        when(userRepository.findByEmail("preyansh@gmail.com"))
                .thenReturn(Optional.of(existingUser));

        // Verify that duplicate email causes an exception.
        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(request)
        );

        // Saving must not happen when the email is already registered.
        verify(userRepository, never()).save(any(User.class));

        // Phone uniqueness is not checked because execution stops at the email check.
        verify(userRepository, never()).existsByPhone(anyString());
    }

    // Test that creation fails when the phone number already exists.
    @Test
    void createUser_shouldThrowExceptionWhenPhoneAlreadyExists() {

        UserRequest request = createUserRequest(
                "Preyansh",
                "preyansh@gmail.com",
                "9876543210"
        );

        // Email is available, but the phone number is already registered.
        when(userRepository.findByEmail("preyansh@gmail.com"))
                .thenReturn(Optional.empty());

        when(userRepository.existsByPhone("9876543210"))
                .thenReturn(true);

        // Verify that duplicate phone number causes an exception.
        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(request)
        );

        // The user should not be saved.
        verify(userRepository, never()).save(any(User.class));
    }

    // Test successfully updating an existing user.
    @Test
    void updateUser_shouldUpdateUserSuccessfully() {

        Long userId = 1L;

        User existingUser = createUser(
                userId,
                "Preyansh",
                "old@gmail.com",
                "9876543210",
                Role.RIDER
        );

        UserRequest request = createUserRequest(
                "Preyansh Sharma",
                "new@gmail.com",
                "9876543211"
        );

        // Return the existing user that needs to be updated.
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));

        // Ensure no other user has the new email or phone number.
        when(userRepository.findByEmailAndIdNot("new@gmail.com", userId))
                .thenReturn(Optional.empty());

        when(userRepository.existsByPhoneAndIdNot("9876543211", userId))
                .thenReturn(false);

        // Return the updated entity when save is called.
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // Call the service method.
        UserResponse result = userService.updateUser(userId, request);

        // Verify that the user's details were updated.
        assertEquals(userId, result.getId());
        assertEquals("Preyansh Sharma", result.getName());
        assertEquals("new@gmail.com", result.getEmail());
        assertEquals("9876543211", result.getPhone());
        assertEquals(Role.RIDER, result.getRole());

        verify(userRepository).save(existingUser);
    }

    // Test that updating fails when the user does not exist.
    @Test
    void updateUser_shouldThrowExceptionWhenUserDoesNotExist() {

        Long userId = 999L;

        UserRequest request = createUserRequest(
                "Preyansh",
                "new@gmail.com",
                "9876543210"
        );

        // The repository cannot find the supplied user ID.
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Verify that UserNotFoundException is thrown.
        assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(userId, request)
        );

        // No update or save should happen.
        verify(userRepository, never()).save(any(User.class));
    }

    // Test that updating fails when another user already has the requested email.
    @Test
    void updateUser_shouldThrowExceptionWhenEmailAlreadyExists() {

        Long userId = 1L;

        User existingUser = createUser(
                userId,
                "Preyansh",
                "old@gmail.com",
                "9876543210",
                Role.RIDER
        );

        User anotherUser = createUser(
                2L,
                "Rahul",
                "new@gmail.com",
                "9876543211",
                Role.RIDER
        );

        UserRequest request = createUserRequest(
                "Preyansh",
                "new@gmail.com",
                "9876543212"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));

        // Another user already owns the requested email.
        when(userRepository.findByEmailAndIdNot("new@gmail.com", userId))
                .thenReturn(Optional.of(anotherUser));

        // Verify that the duplicate email causes an exception.
        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.updateUser(userId, request)
        );

        verify(userRepository, never()).save(any(User.class));

        // Phone uniqueness is not checked after the email check fails.
        verify(userRepository, never())
                .existsByPhoneAndIdNot(anyString(), anyLong());
    }

    // Test that updating fails when another user already has the requested phone.
    @Test
    void updateUser_shouldThrowExceptionWhenPhoneAlreadyExists() {

        Long userId = 1L;

        User existingUser = createUser(
                userId,
                "Preyansh",
                "old@gmail.com",
                "9876543210",
                Role.RIDER
        );

        UserRequest request = createUserRequest(
                "Preyansh",
                "new@gmail.com",
                "9876543211"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));

        // The new email is available.
        when(userRepository.findByEmailAndIdNot("new@gmail.com", userId))
                .thenReturn(Optional.empty());

        // Another user already owns the requested phone number.
        when(userRepository.existsByPhoneAndIdNot("9876543211", userId))
                .thenReturn(true);

        // Verify that duplicate phone number causes an exception.
        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.updateUser(userId, request)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    // Test that a user can retain their own email and phone during an update.
    @Test
    void updateUser_shouldAllowKeepingSameEmailAndPhone() {

        Long userId = 1L;

        User existingUser = createUser(
                userId,
                "Preyansh",
                "preyansh@gmail.com",
                "9876543210",
                Role.RIDER
        );

        UserRequest request = createUserRequest(
                "Preyansh Sharma",
                "PREYANSH@GMAIL.COM",
                "9876543210"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));

        // The repository excludes the current user's ID from the email check.
        when(userRepository.findByEmailAndIdNot("preyansh@gmail.com", userId))
                .thenReturn(Optional.empty());

        // The repository excludes the current user's ID from the phone check.
        when(userRepository.existsByPhoneAndIdNot("9876543210", userId))
                .thenReturn(false);

        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // Call the update method.
        UserResponse result = userService.updateUser(userId, request);

        // Verify that the same email and phone can be retained.
        assertEquals("preyansh@gmail.com", result.getEmail());
        assertEquals("9876543210", result.getPhone());
        assertEquals("Preyansh Sharma", result.getName());

        verify(userRepository).save(existingUser);
    }

    // Test successfully deleting an existing user.
    @Test
    void deleteUser_shouldDeleteUserSuccessfully() {

        Long userId = 1L;

        User existingUser = createUser(
                userId,
                "Preyansh",
                "preyansh@gmail.com",
                "9876543210",
                Role.RIDER
        );

        // Return the existing user before deletion.
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));

        // Call the delete method.
        userService.deleteUser(userId);

        // Verify that the repository's delete method was called.
        verify(userRepository).delete(existingUser);
    }

    // Test deleting a user when the supplied ID does not exist.
    @Test
    void deleteUser_shouldThrowExceptionWhenUserDoesNotExist() {

        Long userId = 999L;

        // Indicate that no user exists with the supplied ID.
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Verify that the service throws UserNotFoundException.
        assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(userId)
        );

        // Verify that deletion never happens.
        verify(userRepository, never()).delete(any(User.class));
    }

    // Test partially updating only the user's name.
    @Test
    void patchUser_shouldUpdateNameOnlySuccessfully() {

        Long userId = 1L;

        User existingUser = createUser(
                userId,
                "Preyansh",
                "preyansh@gmail.com",
                "9876543210",
                Role.RIDER
        );

        UserPatchRequest request = createUserPatchRequest(
                "Preyansh Sharma",
                null,
                null
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // Call the partial update method.
        UserResponse result = userService.patchUser(userId, request);

        // Verify that the name changed while email and phone remained unchanged.
        assertEquals("Preyansh Sharma", result.getName());
        assertEquals("preyansh@gmail.com", result.getEmail());
        assertEquals("9876543210", result.getPhone());

        verify(userRepository).save(existingUser);

        // No email or phone checks should happen for a name-only update.
        verify(userRepository, never()).findByEmailAndIdNot(anyString(), anyLong());
        verify(userRepository, never())
                .existsByPhoneAndIdNot(anyString(), anyLong());
    }

    // Test partially updating only the user's email.
    @Test
    void patchUser_shouldUpdateEmailOnlySuccessfully() {

        Long userId = 1L;

        User existingUser = createUser(
                userId,
                "Preyansh",
                "old@gmail.com",
                "9876543210",
                Role.RIDER
        );

        UserPatchRequest request = createUserPatchRequest(
                null,
                "  NEW@GMAIL.COM  ",
                null
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));

        // Verify that the normalized email is not owned by another user.
        when(userRepository.findByEmailAndIdNot("new@gmail.com", userId))
                .thenReturn(Optional.empty());

        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // Call the partial update method.
        UserResponse result = userService.patchUser(userId, request);

        // Verify that the email changed and other fields remained unchanged.
        assertEquals("new@gmail.com", result.getEmail());
        assertEquals("Preyansh", result.getName());
        assertEquals("9876543210", result.getPhone());

        verify(userRepository).save(existingUser);

        // Phone uniqueness should not be checked for an email-only update.
        verify(userRepository, never())
                .existsByPhoneAndIdNot(anyString(), anyLong());
    }

    // Test partially updating only the user's phone number.
    @Test
    void patchUser_shouldUpdatePhoneOnlySuccessfully() {

        Long userId = 1L;

        User existingUser = createUser(
                userId,
                "Preyansh",
                "preyansh@gmail.com",
                "9876543210",
                Role.RIDER
        );

        UserPatchRequest request = createUserPatchRequest(
                null,
                null,
                "9876543211"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));

        // Verify that the phone number is not owned by another user.
        when(userRepository.existsByPhoneAndIdNot("9876543211", userId))
                .thenReturn(false);

        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // Call the partial update method.
        UserResponse result = userService.patchUser(userId, request);

        // Verify that only the phone number changed.
        assertEquals("9876543211", result.getPhone());
        assertEquals("Preyansh", result.getName());
        assertEquals("preyansh@gmail.com", result.getEmail());

        verify(userRepository).save(existingUser);

        // Email uniqueness should not be checked for a phone-only update.
        verify(userRepository, never())
                .findByEmailAndIdNot(anyString(), anyLong());
    }

    // Test partially updating multiple user fields together.
    @Test
    void patchUser_shouldUpdateMultipleFieldsSuccessfully() {

        Long userId = 1L;

        User existingUser = createUser(
                userId,
                "Preyansh",
                "old@gmail.com",
                "9876543210",
                Role.RIDER
        );

        UserPatchRequest request = createUserPatchRequest(
                "Preyansh Sharma",
                "NEW@GMAIL.COM",
                "9876543211"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.findByEmailAndIdNot("new@gmail.com", userId))
                .thenReturn(Optional.empty());

        when(userRepository.existsByPhoneAndIdNot("9876543211", userId))
                .thenReturn(false);

        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // Call the partial update method with all three fields supplied.
        UserResponse result = userService.patchUser(userId, request);

        // Verify that all supplied fields were updated.
        assertEquals("Preyansh Sharma", result.getName());
        assertEquals("new@gmail.com", result.getEmail());
        assertEquals("9876543211", result.getPhone());

        verify(userRepository).save(existingUser);
    }

    // Test that a partial update fails when the requested email belongs to another user.
    @Test
    void patchUser_shouldThrowExceptionWhenEmailAlreadyExists() {

        Long userId = 1L;

        User existingUser = createUser(
                userId,
                "Preyansh",
                "old@gmail.com",
                "9876543210",
                Role.RIDER
        );

        User anotherUser = createUser(
                2L,
                "Rahul",
                "new@gmail.com",
                "9876543211",
                Role.RIDER
        );

        UserPatchRequest request = createUserPatchRequest(
                null,
                "new@gmail.com",
                null
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));

        // Another user already owns the requested email.
        when(userRepository.findByEmailAndIdNot("new@gmail.com", userId))
                .thenReturn(Optional.of(anotherUser));

        // Verify that the duplicate email causes an exception.
        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.patchUser(userId, request)
        );

        // The entity must not be saved when the email is duplicated.
        verify(userRepository, never()).save(any(User.class));

        // Phone uniqueness is not checked because no phone was supplied.
        verify(userRepository, never())
                .existsByPhoneAndIdNot(anyString(), anyLong());
    }

    // Test that a partial update fails when the requested phone belongs to another user.
    @Test
    void patchUser_shouldThrowExceptionWhenPhoneAlreadyExists() {

        Long userId = 1L;

        User existingUser = createUser(
                userId,
                "Preyansh",
                "preyansh@gmail.com",
                "9876543210",
                Role.RIDER
        );

        UserPatchRequest request = createUserPatchRequest(
                null,
                null,
                "9876543211"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));

        // Another user already owns the requested phone number.
        when(userRepository.existsByPhoneAndIdNot("9876543211", userId))
                .thenReturn(true);

        // Verify that the duplicate phone causes an exception.
        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.patchUser(userId, request)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    // Test partially updating a user when the supplied ID does not exist.
    @Test
    void patchUser_shouldThrowExceptionWhenUserDoesNotExist() {

        Long userId = 999L;

        UserPatchRequest request = createUserPatchRequest(
                "Preyansh Sharma",
                null,
                null
        );

        // The repository cannot find the requested user.
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Verify that UserNotFoundException is thrown.
        assertThrows(
                UserNotFoundException.class,
                () -> userService.patchUser(userId, request)
        );

        // No update should be saved.
        verify(userRepository, never()).save(any(User.class));
    }

    // Test searching for users by a matching name.
    @Test
    void searchUsersByName_shouldReturnMatchingUsersSuccessfully() {

        String searchName = "pre";

        User user1 = createUser(
                1L,
                "Preyansh",
                "preyansh@gmail.com",
                "9876543210",
                Role.RIDER
        );

        User user2 = createUser(
                2L,
                "Prem",
                "prem@gmail.com",
                "9876543211",
                Role.RIDER
        );

        // Mock the repository to return users matching the search text.
        when(userRepository.findByNameContainingIgnoreCase(searchName))
                .thenReturn(List.of(user1, user2));

        // Call the search method.
        List<UserResponse> result = userService.searchUsersByName(searchName);

        // Verify that both matching users are returned as response DTOs.
        assertEquals(2, result.size());
        assertEquals("Preyansh", result.get(0).getName());
        assertEquals("Prem", result.get(1).getName());

        verify(userRepository).findByNameContainingIgnoreCase(searchName);
    }

    // Test searching for users when no matching name is found.
    @Test
    void searchUsersByName_shouldReturnEmptyListWhenNoUsersMatch() {

        String searchName = "unknown";

        // Mock the repository to return no matching users.
        when(userRepository.findByNameContainingIgnoreCase(searchName))
                .thenReturn(List.of());

        // Call the search method.
        List<UserResponse> result = userService.searchUsersByName(searchName);

        // Verify that an empty list is returned.
        assertTrue(result.isEmpty());

        verify(userRepository).findByNameContainingIgnoreCase(searchName);
    }

    // Test searching for users by a matching email.
    @Test
    void searchUsersByEmail_shouldReturnMatchingUsersSuccessfully() {

        String searchEmail = "gmail";

        User user1 = createUser(
                1L,
                "Preyansh",
                "preyansh@gmail.com",
                "9876543210",
                Role.RIDER
        );

        User user2 = createUser(
                2L,
                "Rahul",
                "rahul@gmail.com",
                "9876543211",
                Role.DRIVER
        );

        // Mock the repository to return users matching the email text.
        when(userRepository.findByEmailContainingIgnoreCase(searchEmail))
                .thenReturn(List.of(user1, user2));

        // Call the search method.
        List<UserResponse> result = userService.searchUsersByEmail(searchEmail);

        // Verify that matching users are returned as response DTOs.
        assertEquals(2, result.size());
        assertEquals("preyansh@gmail.com", result.get(0).getEmail());
        assertEquals("rahul@gmail.com", result.get(1).getEmail());

        verify(userRepository).findByEmailContainingIgnoreCase(searchEmail);
    }

    // Test searching for users when no matching email is found.
    @Test
    void searchUsersByEmail_shouldReturnEmptyListWhenNoUsersMatch() {

        String searchEmail = "unknown";

        // Mock the repository to return no matching users.
        when(userRepository.findByEmailContainingIgnoreCase(searchEmail))
                .thenReturn(List.of());

        // Call the search method.
        List<UserResponse> result = userService.searchUsersByEmail(searchEmail);

        // Verify that an empty list is returned.
        assertTrue(result.isEmpty());

        verify(userRepository).findByEmailContainingIgnoreCase(searchEmail);
    }

    // Creates a User entity with the supplied test data.
    private User createUser(
            Long id,
            String name,
            String email,
            String phone,
            Role role
    ) {
        User user = new User();

        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);

        return user;
    }

    // Creates a UserRequest object for create and full-update tests.
    private UserRequest createUserRequest(
            String name,
            String email,
            String phone
    ) {
        UserRequest request = new UserRequest();

        request.setName(name);
        request.setEmail(email);
        request.setPhone(phone);

        return request;
    }

    // Creates a UserPatchRequest object for partial-update tests.
    private UserPatchRequest createUserPatchRequest(
            String name,
            String email,
            String phone
    ) {
        UserPatchRequest request = new UserPatchRequest();

        request.setName(name);
        request.setEmail(email);
        request.setPhone(phone);

        return request;
    }
}
