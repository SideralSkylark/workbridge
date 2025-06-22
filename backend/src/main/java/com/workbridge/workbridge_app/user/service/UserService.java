package com.workbridge.workbridge_app.user.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.workbridge.workbridge_app.user.dto.ProviderRequestDTO;
import com.workbridge.workbridge_app.user.dto.UpdateUserProfileDTO;
import com.workbridge.workbridge_app.user.dto.UserResponseDTO;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.entity.ProviderRequest;
import com.workbridge.workbridge_app.user.entity.UserRole;
import com.workbridge.workbridge_app.user.entity.UserRoleEntity;
import com.workbridge.workbridge_app.user.exception.ProviderRequestNotFoundException;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.mapper.UserMapper;
import com.workbridge.workbridge_app.user.repository.ProviderRequestRepository;
import com.workbridge.workbridge_app.user.repository.UserRepository;
import com.workbridge.workbridge_app.user.repository.UserRoleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Service class for managing user-related operations, including user retrieval, updates, role management,
 * provider requests, and account status changes. This service acts as the main business logic layer for user
 * management, interacting with repositories and mapping entities to DTOs for API responses.
 *
 * <p>Key responsibilities include:</p>
 * <ul>
 *   <li>Retrieving users (all, by role, or non-admins)</li>
 *   <li>Managing user accounts (create, update, delete, enable/disable)</li>
 *   <li>Handling provider requests (submit, approve, check status)</li>
 *   <li>Mapping between entities and DTOs</li>
 *   <li>Throwing domain-specific exceptions for error handling</li>
 * </ul>
 *
 * <p>Transactional methods ensure data consistency for operations that modify user or provider request state.</p>
 *
 * <p>Typical usage:</p>
 * <pre>
 *   userService.getAllUsers();
 *   userService.updateUser(username, userDto);
 *   userService.requestToBecomeProvider(username);
 *   userService.approveProviderRequest(requestId);
 * </pre>
 *
 * @author Workbridge Team
 * @since 2025-06-22
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ProviderRequestRepository providerRequestRepository;

    /**
     * Retrieves all users from the database.
     *
     * @return List of UserResponseDTO objects representing all users.
     */
    public List<UserResponseDTO> getAllUsers() {
        return mapToUserResponseDTO(userRepository.findAll());
    }

    /**
     * Retrieves all non-admin users from the database.
     *
     * @return List of UserResponseDTO objects for users without the ADMIN role.
     */
    public List<UserResponseDTO> getAllNonAdminUsers() {
        return mapToUserResponseDTO(
            userRepository.findAll().stream()
                .filter(user -> user.lacksRole(UserRole.ADMIN))
                .toList());
    }

    /**
     * Retrieves users by a specific role.
     *
     * @param role The role to filter users by.
     * @return List of UserResponseDTO objects for users with the specified role.
     */
    public List<UserResponseDTO> getUsersByRole(UserRole role) {
        return mapToUserResponseDTO(
            userRepository.findAll().stream()
                .filter(user ->user.hasRole(role))
                .toList());
    }

    /**
     * Retrieves all pending provider requests (not yet approved).
     *
     * @return List of ProviderRequestDTO objects for pending requests.
     */
    @Transactional
    public List<ProviderRequestDTO> getAllProviderRequestNotApproved() {
        return mapToProviderRequestDTO(
            providerRequestRepository.findAll().stream()
                .filter(pr -> !pr.isApproved())
                .toList());
    }

    /**
     * Finds a user by their unique ID.
     *
     * @param id The user ID.
     * @return Optional containing the user if found, or empty if not found.
     */
    public Optional<ApplicationUser> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Finds a user by their username.
     *
     * @param username The username to search for.
     * @return Optional containing the user if found, or empty if not found.
     */
    public Optional<ApplicationUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Finds a user by their email address.
     *
     * @param email The email to search for.
     * @return Optional containing the user if found, or empty if not found.
     */
    public Optional<ApplicationUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Saves a user to the database (create or update).
     *
     * @param user The ApplicationUser entity to save.
     * @return The saved ApplicationUser entity.
     */
    public ApplicationUser saveUser(ApplicationUser user) {
        return userRepository.save(user);
    }

    /**
     * Updates a user's information using validated profile update data.
     *
     * @param username The username of the user to update.
     * @param dto The validated update profile DTO.
     * @return The updated user.
     * @throws UserNotFoundException if the user is not found.
     */
    @Transactional
    public ApplicationUser updateUser(String username, UpdateUserProfileDTO dto) {
        ApplicationUser user = getUser(username);
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setEnabled(dto.isEnabled());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Deletes a user by their unique ID.
     *
     * @param id The user ID.
     */
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Deletes a user by their username.
     *
     * @param username The username of the user to delete.
     * @throws UserNotFoundException if the user is not found.
     */
    @Transactional
    public void deleteByUsername(String username) {
        userRepository.delete(getUser(username));
        //TODO: delete related entities
    }

    /**
     * Enables a user account by email.
     *
     * @param email The user's email address.
     * @return true if the account was enabled, false if it was already enabled.
     * @throws UserNotFoundException if the user is not found.
     */
    @Transactional
    public boolean enableAccount(String email) {
        return updateAccountStatus(email, true);
    }

    /**
     * Disables a user account by email.
     *
     * @param email The user's email address.
     * @return true if the account was disabled, false if it was already disabled.
     * @throws UserNotFoundException if the user is not found.
     */
    @Transactional
    public boolean disableAccount(String email) {
        return updateAccountStatus(email, false);
    }

    /**
     * Updates a user's account status (enable/disable) by email.
     *
     * @param email The user's email address.
     * @param enable Whether to enable (true) or disable (false) the account.
     * @return true if the status was changed, false if it was already in the desired state.
     * @throws UserNotFoundException if the user is not found.
     */
    private boolean updateAccountStatus(String email, boolean enable) {
        ApplicationUser user = getUserByEmailOrThrow(email);
        if (user.isEnabled() == enable) return false;

        user.setEnabled(enable);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    /**
     * Submits a request for a user to become a service provider.
     *
     * @param username The username of the user requesting provider status.
     * @throws UserNotFoundException if the user is not found.
     * @throws IllegalStateException if the user is already a service provider.
     */
    @Transactional
    public void requestToBecomeProvider(String username) {
        ApplicationUser user = getUser(username);
        if (user.hasRole(UserRole.SERVICE_PROVIDER)) {
            throw new IllegalStateException("User is already a service provider.");
        }

        providerRequestRepository.save(new ProviderRequest(user));
        //TODO: notify admin
    }

    /**
     * Approves a pending provider request and grants the SERVICE_PROVIDER role to the user.
     *
     * @param requestId The ID of the provider request to approve.
     * @throws ProviderRequestNotFoundException if the request is not found.
     * @throws IllegalStateException if the request is already approved or the SERVICE_PROVIDER role is missing.
     */
    @Transactional
    public void approveProviderRequest(Long requestId) {
        ProviderRequest request = getProviderRequest(requestId);

        validateNotApproved(request);
        grantServiceProviderRole(request.getUser());
        request.markApproved(LocalDateTime.now());
        providerRequestRepository.save(request);
        // TODO: notify user
    }

    /**
     * Checks if a user has a pending provider request.
     *
     * @param username The username to check.
     * @return true if the user has a pending request, false otherwise.
     * @throws UserNotFoundException if the user is not found.
     */
    public boolean hasPendingProviderRequest(String username) {
       return providerRequestRepository.existsByUserAndApprovedFalse(getUser(username));
    }
    
    /**
     * Checks if a user is a service provider.
     *
     * @param username The username to check.
     * @return true if the user is a service provider, false otherwise.
     * @throws UserNotFoundException if the user is not found.
     */
    public boolean isServiceProvider(String username) {
        return getUser(username).isServiceProvider();
    }

    /**
     * Converts a list of ApplicationUser entities to a list of UserResponseDTOs.
     *
     * @param users The list of ApplicationUser entities to convert.
     * @return List of UserResponseDTO objects.
     */
    private List<UserResponseDTO> mapToUserResponseDTO(List<ApplicationUser> users) {
        return users.stream().map(userMapper::toDTO).toList();
    }

    /**
     * Converts a list of ProviderRequest entities to a list of ProviderRequestDTOs.
     *
     * @param requests The list of ProviderRequest entities to convert.
     * @return List of ProviderRequestDTO objects.
     */
    private List<ProviderRequestDTO> mapToProviderRequestDTO(List<ProviderRequest> requests) {
        return requests.stream().map(userMapper::toDTO).toList();
    }

    /**
     * Helper method to get a user by username or throw a UserNotFoundException.
     *
     * @param username The username to search for.
     * @return The ApplicationUser entity.
     * @throws UserNotFoundException if the user is not found.
     */
    private ApplicationUser getUser(String username) {
        return getOrThrow(
            username, 
            userRepository::findByUsername,
            "No user found with username: " + username);
    }

    /**
     * Helper method to get a user by email or throw a UserNotFoundException.
     *
     * @param email The email to search for.
     * @return The ApplicationUser entity.
     * @throws UserNotFoundException if the user is not found.
     */
    private ApplicationUser getUserByEmailOrThrow(String email) {
        return getOrThrow(email, 
        userRepository::findByEmail, 
        "No user found with email: " + email);
    }

    /**
     * Generic helper to get an ApplicationUser by a value using a finder function, or throw a UserNotFoundException.
     *
     * @param value The value to search for (username or email).
     * @param finder The function to find the user.
     * @param errorMessage The error message for the exception.
     * @return The ApplicationUser entity.
     * @throws UserNotFoundException if the user is not found.
     */
    private <T> ApplicationUser getOrThrow(
        T value,
        Function<T, Optional<ApplicationUser>> finder,
        String errorMessage) {

        return finder.apply(value)
                    .orElseThrow(() -> new UserNotFoundException(errorMessage));                   
    }

    /**
     * Helper method to get a ProviderRequest by ID or throw a ProviderRequestNotFoundException.
     *
     * @param id The provider request ID.
     * @return The ProviderRequest entity.
     * @throws ProviderRequestNotFoundException if the request is not found.
     */
    private ProviderRequest getProviderRequest(Long id) {
        return providerRequestRepository.findById(id)
                .orElseThrow(() -> new ProviderRequestNotFoundException(
                        "No provider request found with id: " + id));
    }

    /**
     * Validates that a provider request has not already been approved.
     *
     * @param request The ProviderRequest to validate.
     * @throws IllegalStateException if the request is already approved.
     */
    private void validateNotApproved(ProviderRequest request) {
        if (request.isApproved()) throw new IllegalStateException("Request already approved.");
    }

    /**
     * Grants the SERVICE_PROVIDER role to a user.
     *
     * @param user The ApplicationUser to grant the role to.
     * @throws IllegalStateException if the SERVICE_PROVIDER role is missing in the database.
     */
    private void grantServiceProviderRole(ApplicationUser user) {
        UserRoleEntity spRole = userRoleRepository.findByRole(UserRole.SERVICE_PROVIDER)
                .orElseThrow(() -> new IllegalStateException("SERVICE_PROVIDER role missing in DB"));
        user.addRole(spRole);
        userRepository.save(user);
    }
}
