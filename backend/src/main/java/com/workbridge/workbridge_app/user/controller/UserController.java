package com.workbridge.workbridge_app.user.controller;

import com.workbridge.workbridge_app.user.dto.UpdateUserProfileDTO;
import com.workbridge.workbridge_app.user.dto.UserResponseDTO;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.mapper.UserMapper;
import com.workbridge.workbridge_app.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing actions related to the authenticated user.
 * <p>
 * This controller provides endpoints for:
 * <ul>
 *   <li>Retrieving the logged-in user's profile</li>
 *   <li>Updating user details</li>
 *   <li>Deleting user account</li>
 *   <li>Requesting to become a service provider</li>
 *   <li>Checking the status of a provider request</li>
 * </ul>
 *
 * <p>All endpoints require a valid authenticated session. Role-based
 * access control is enforced where applicable.</p>
 * 
 * @author Workbridge Team
 * 
 * @since 2025-06-22
 */
@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Get the profile of the currently authenticated user.
     * <p>
     * Retrieves user details based on the authentication context. 
     * Converts the internal {@link ApplicationUser} to {@link UserResponseDTO}.
     *
     * @return 200 OK with the user data if authenticated and found
     * @throws UserNotFoundException if the user cannot be found in the database
     * @throws IllegalStateException if the authentication context is invalid
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getUserDetails() {
        ApplicationUser user = userService.findByUsername(getAuthenticatedUsername())
                                          .orElseThrow(() -> new UserNotFoundException("User not found"));
        return ResponseEntity.ok(userMapper.toDTO(user));
    }

    /**
     * Update the authenticated user's profile information.
     * <p>
     * Updates basic fields like username, email, and enabled status.
     * Note that only authenticated users can update their own data.
     *
     * @param payload A {@link UpdateUserProfileDTO} containing validated updated fields
     * @return 200 OK with the updated user data
     * @throws UserNotFoundException if the user cannot be found
     * @throws IllegalStateException if the authentication context is invalid
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateUserDetails(@Valid @RequestBody UpdateUserProfileDTO payload) {
        ApplicationUser updated = userService.updateUser(
            getAuthenticatedUsername(), 
            payload);
        return ResponseEntity.ok(userMapper.toDTO(updated));
    }

    /**
     * Permanently delete the currently authenticated user's account.
     * <p>
     * This operation is irreversible and requires valid authentication.
     *
     * @return 204 No Content if deletion was successful
     * @throws UserNotFoundException if the user cannot be found
     * @throws IllegalStateException if the authentication context is invalid
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser() {
        userService.deleteByUsername(getAuthenticatedUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * Submit a request to become a service provider.
     * <p>
     * Only users with the role {@code ROLE_SERVICE_SEEKER} are allowed to access this endpoint.
     * If the user already has the provider role or has a pending request, the call fails.
     *
     * @return 200 OK with a success message
     * @throws IllegalStateException if the user already is a provider or has a pending request
     * @throws UserNotFoundException if the user cannot be found
     */
    @PreAuthorize("hasRole('ROLE_SERVICE_SEEKER')")
    @PostMapping("/me/request-to-become-provider")
    public ResponseEntity<Map<String, String>> requestToBecomeProvider() {
        String username = getAuthenticatedUsername();
        userService.requestToBecomeProvider(username);
        return ResponseEntity.ok(Map.of(
            "message", 
            "Request to become a service provider sent successfully."));
    }

    /**
     * Check the status of the current user's provider request.
     * <p>
     * Returns whether the user has a pending request and/or is already approved as a provider.
     *
     * @return 200 OK with a map containing:
     *         <ul>
     *             <li>{@code requested} - true if a request is pending</li>
     *             <li>{@code approved}  - true if the user is already a service provider</li>
     *         </ul>
     * @throws UserNotFoundException if the user cannot be found
     */
    @GetMapping("/me/provider-request-status")
    public ResponseEntity<Map<String, Boolean>> getProviderRequestStatus() {
        String username = getAuthenticatedUsername();
        boolean requested = userService.hasPendingProviderRequest(username);
        boolean approved  = userService.isServiceProvider(username);
        return ResponseEntity.ok(Map.of(
            "requested", requested, 
            "approved", approved));
    } //TODO: consider changing to /me/provider-request/status 

    /**
     * Helper method to extract the username of the currently authenticated user.
     * <p>
     * This method verifies that the authentication object exists and is valid.
     *
     * @return the username from the security context
     * @throws IllegalStateException if no authenticated user is found
     */
    private String getAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Authenticated user not found");
        }
        String username = auth.getName();
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Authenticated user not found");
        }
        return username;
    } //TODO:Consider moving to a utility class
}