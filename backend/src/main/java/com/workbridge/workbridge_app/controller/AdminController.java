package com.workbridge.workbridge_app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.workbridge.workbridge_app.dto.ProviderRequestDTO;
import com.workbridge.workbridge_app.dto.UserResponseDTO;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller responsible for administrative operations in the system.
 * This controller provides endpoints for:
 * - Managing user accounts (retrieval, updates, deletion)
 * - Managing provider requests
 * - System-wide operations
 * 
 * All endpoints require ADMIN role authorization.
 * The controller includes comprehensive error handling and logging.
 */
@RestController
@RequestMapping("/api/v1/admins")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final UserService userService;

    /**
     * Retrieves a list of all users in the system.
     * This endpoint:
     * 1. Fetches all users from the database
     * 2. Converts each user to a DTO
     * 3. Returns the list of user DTOs
     *
     * @return ResponseEntity containing a list of UserResponseDTO objects
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping()
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserResponseDTO> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error retrieving all users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred while retrieving users. Please try again later.");
        }
    }

    /**
     * Retrieves a list of all non-admin users in the system.
     * This endpoint:
     * 1. Fetches all non-admin users from the database
     * 2. Converts each user to a DTO
     * 3. Returns the list of user DTOs
     *
     * @return ResponseEntity containing a list of UserResponseDTO objects
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/nonAdmin")
    public ResponseEntity<?> getAllNonAdminUsers() {
        try {
            List<UserResponseDTO> nonAdmins = userService.getAllNonAdminUsers();
            return ResponseEntity.ok(nonAdmins);
        } catch (Exception e) {
            log.error("Error retrieving non-admin users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred while retrieving non-admin users. Please try again later.");
        }
    }

    /**
     * Retrieves a list of all pending provider requests.
     * This endpoint:
     * 1. Fetches all pending provider requests from the database
     * 2. Converts each request to a DTO
     * 3. Returns the list of provider request DTOs
     *
     * @return ResponseEntity containing a list of ProviderRequestDTO objects
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/provider-requests")
    public ResponseEntity<?> getAllProviderRequestsNotApproved() {
        try {
            List<ProviderRequestDTO> providerRequests = userService.getAllProviderRequestNotApproved();
            return ResponseEntity.ok(providerRequests);
        } catch (Exception e) {
            log.error("Error retrieving provider requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred while retrieving provider requests. Please try again later.");
        }
    }

    /**
     * Enables a user account.
     * This endpoint:
     * 1. Looks up the user by email
     * 2. Enables the user's account
     * 3. Returns a success message
     *
     * @param email The email of the user to enable
     * @return ResponseEntity containing a success message
     * @throws UserNotFoundException if the user is not found
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/enable")
    public ResponseEntity<?> enableUserAccount(@RequestParam String email) {
        return updateAccountStatus(email, true);
    }

    /**
     * Disables a user account.
     * This endpoint:
     * 1. Looks up the user by email
     * 2. Disables the user's account
     * 3. Returns a success message
     *
     * @param email The email of the user to disable
     * @return ResponseEntity containing a success message
     * @throws UserNotFoundException if the user is not found
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/disable")
    public ResponseEntity<?> disableUserAccount(@RequestParam String email) {
        return updateAccountStatus(email, false);
    }

    /**
     * Approves a provider request.
     * This endpoint:
     * 1. Looks up the provider request by ID
     * 2. Approves the request
     * 3. Returns a success message
     *
     * @param requestId The ID of the provider request to approve
     * @return ResponseEntity containing a success message
     * @throws UserNotFoundException if the request is not found
     * @throws IllegalStateException if the request is not pending
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/approve-provider/{requestId}")
    public ResponseEntity<?> approveProviderRole(@PathVariable Long requestId) {
        try {
            userService.approveProviderRequest(requestId);
            return ResponseEntity.ok(Map.of("message", "Provider request approved successfully."));
        } catch (UserNotFoundException e) {
            log.warn("Provider request approval failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Provider request approval failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error approving provider request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred while approving the provider request. Please try again later.");
        }
    }

    /**
     * Helper method to update user account status
     * @param email The email of the user
     * @param enable Whether to enable or disable the account
     * @return ResponseEntity with appropriate status and message
     */
    private ResponseEntity<?> updateAccountStatus(String email, boolean enable) {
        try {
            boolean statusUpdated = enable ? userService.enableAccount(email) : userService.disableAccount(email);
            if (statusUpdated) {
                return ResponseEntity.ok(Map.of("message", 
                    String.format("User account %s successfully.", enable ? "enabled" : "disabled")));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", 
                        String.format("User not found or already %s.", enable ? "enabled" : "disabled")));
            }
        } catch (UserNotFoundException e) {
            log.warn("Account status update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "User with the specified email does not exist."));
        } catch (Exception e) {
            log.error("Error updating account status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "An unexpected error occurred while updating the account status. Please try again later."));
        }
    }
}
