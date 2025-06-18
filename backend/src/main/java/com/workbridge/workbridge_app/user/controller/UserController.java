package com.workbridge.workbridge_app.user.controller;

import com.workbridge.workbridge_app.user.dto.UserResponseDTO;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for managing user-related operations.
 * This controller provides endpoints for:
 * - Retrieving user details
 * - Updating user information
 * - Deleting user accounts
 * - Managing provider requests
 * 
 * All endpoints require authentication and appropriate role-based authorization.
 * The controller includes comprehensive error handling and logging.
 */
@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Retrieves the details of the currently authenticated user.
     * This endpoint:
     * 1. Extracts the username from the security context
     * 2. Retrieves the user details from the database
     * 3. Returns the user information as a DTO
     *
     * @return ResponseEntity containing the user details as UserResponseDTO
     * @throws UserNotFoundException if the user is not found
     * @throws IllegalStateException if no authenticated user is found in the security context
     */
    @GetMapping("/me")
    public ResponseEntity<?> getUserDetails() {
        try {
            String username = getAuthenticatedUsername();
            ApplicationUser user = userService.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            return ResponseEntity.ok(userService.convertToDTO(user));
        } catch (UserNotFoundException e) {
            log.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (java.lang.IllegalStateException e) {
            log.warn("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving user details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Updates the details of the currently authenticated user.
     * This endpoint:
     * 1. Extracts the username from the security context
     * 2. Updates the user information in the database
     * 3. Returns the updated user information
     *
     * @param userResponseDTO The updated user information
     * @return ResponseEntity containing the updated user details as UserResponseDTO
     * @throws UserNotFoundException if the user is not found
     * @throws IllegalStateException if no authenticated user is found in the security context
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateUserDetails(@RequestBody UserResponseDTO userResponseDTO) {
        try {
            String username = getAuthenticatedUsername();
            ApplicationUser updatedUser = userService.updateUser(username, userResponseDTO);
            return ResponseEntity.ok(userService.convertToDTO(updatedUser));
        } catch (UserNotFoundException e) {
            log.warn("User update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (java.lang.IllegalStateException e) {
            log.warn("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating user details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Deletes the account of the currently authenticated user.
     * This endpoint:
     * 1. Extracts the username from the security context
     * 2. Deletes the user from the database
     * 3. Returns a no-content response on success
     *
     * @return ResponseEntity with no content on success
     * @throws UserNotFoundException if the user is not found
     * @throws IllegalStateException if no authenticated user is found in the security context
     */
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser() {
        try {
            String username = getAuthenticatedUsername();
            userService.deleteByUsername(username);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            log.warn("User deletion failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (java.lang.IllegalStateException e) {
            log.warn("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Submits a request for the current user to become a service provider.
     * This endpoint:
     * 1. Extracts the username from the security context
     * 2. Creates a provider request for the user
     * 3. Returns a success message
     *
     * @return ResponseEntity containing a success message
     * @throws UserNotFoundException if the user is not found
     * @throws IllegalStateException if the user is not a service seeker or already has a pending request
     */
    @PreAuthorize("hasRole('ROLE_SERVICE_SEEKER')")
    @PostMapping("/me/request-to-become-provider")
    public ResponseEntity<?> requestToBecomeProvider() {
        try {
            String username = getAuthenticatedUsername();
            userService.requestToBecomeProvider(username);
            return ResponseEntity.ok(Map.of("message", "Request to become a service provider sent successfully."));
        } catch (UserNotFoundException e) {
            log.warn("Provider request failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (java.lang.IllegalStateException e) {
            log.warn("Provider request failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error requesting to become provider", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Retrieves the status of the current user's provider request.
     * This endpoint:
     * 1. Extracts the username from the security context
     * 2. Checks if the user has a pending provider request
     * 3. Checks if the user is already a service provider
     * 4. Returns the request status information
     *
     * @return ResponseEntity containing a map with request status information
     * @throws UserNotFoundException if the user is not found
     * @throws IllegalStateException if no authenticated user is found in the security context
     */
    @GetMapping("/me/provider-request-status")
    public ResponseEntity<?> getProviderRequestStatus() {
        try {
            String username = getAuthenticatedUsername();
            boolean hasPendingRequest = userService.hasPendingProviderRequest(username);
            boolean isProvider = userService.isServiceProvider(username);
            return ResponseEntity.ok(Map.of(
                "requested", hasPendingRequest,
                "approved", isProvider
            ));
        } catch (UserNotFoundException e) {
            log.warn("Provider status check failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (java.lang.IllegalStateException e) {
            log.warn("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error checking provider request status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Helper method to extract the username of the currently authenticated user.
     * This method:
     * 1. Retrieves the authentication object from the security context
     * 2. Extracts the username from the authentication object
     * 3. Validates that a username was found
     *
     * @return The authenticated username
     * @throws IllegalStateException if no authenticated user is found in the security context
     */
    private String getAuthenticatedUsername() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || username.isBlank()) {
            throw new java.lang.IllegalStateException("Authenticated user not found.");
        }
        return username;
    }
}