package com.workbridge.workbridge_app.user.controller;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.workbridge.workbridge_app.user.dto.ProviderRequestDTO;
import com.workbridge.workbridge_app.user.dto.UserResponseDTO;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller responsible for administrative operations in the system.
 * <p>
 * This controller provides endpoints for:
 * <ul>
 *   <li>Managing user accounts (retrieval, updates, deletion)</li>
 *   <li>Managing provider requests (approval, listing pending requests)</li>
 *   <li>System-wide administrative operations</li>
 * </ul>
 *
 * <p>All endpoints require <b>ADMIN</b> role authorization. The controller includes comprehensive error handling
 * and logging, and is designed for use by system administrators only.</p>
 *
 * <p>Typical usage:</p>
 * <pre>
 *   GET    /api/v1/admins/users                // List all users
 *   GET    /api/v1/admins/non-admin-users      // List all non-admin users
 *   GET    /api/v1/admins/provider-requests/pending // List pending provider requests
 *   PATCH  /api/v1/admins/provider-requests/{id}/approve // Approve provider request
 *   PATCH  /api/v1/admins/users/{email}/enable // Enable user account
 *   PUT    /api/v1/admins/users/{email}/disable    // Disable user account
 * </pre>
 *
 * <p>All responses are wrapped in {@link ResponseEntity} and use DTOs for data transfer.</p>
 *
 * @author Workbridge Team
 * @since 2025-06-22
 */
@RestController
@RequestMapping("/api/v1/admins")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final UserService userService;

    /**
     * Retrieves a paginated list of all users in the system.
     * <p>
     * This endpoint fetches users from the database, converts them to DTOs, and returns a paginated list.
     *
     * @param page the page number (0-based, default 0)
     * @param size the page size (default 20)
     * @return ResponseEntity containing a page of UserResponseDTO objects
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getAllUsers(
            @PageableDefault(
                page = 0,
                size = 20, 
                sort = "id", 
                direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(
            new ApiResponse<>(
                userService.getAllUsers(pageable), 
                "Fetched users successfully")
        );
    }

    /**
     * Retrieves a paginated list of all non-admin users in the system.
     * <p>
     * This endpoint fetches non-admin users from the database, converts them to DTOs, and returns a paginated list.
     *
     * @param page the page number (0-based, default 0)
     * @param size the page size (default 20)
     * @return ResponseEntity containing a page of UserResponseDTO objects
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/non-admin-users")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getAllNonAdminUsers(
            @PageableDefault(
                page = 0,
                size = 20,
                sort = "id",
                direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(
            new ApiResponse<>(
                userService.getAllNonAdminUsers(pageable),
                "Fetched non-admin users successfully")
        );
    }

    /**
     * Retrieves a paginated list of all pending provider requests.
     * <p>
     * This endpoint fetches provider requests that have not yet been approved, converts them to DTOs, and returns a paginated list.
     *
     * @param page the page number (0-based, default 0)
     * @param size the page size (default 20)
     * @return ResponseEntity containing a page of ProviderRequestDTO objects
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/provider-requests/pending")
    public ResponseEntity<ApiResponse<Page<ProviderRequestDTO>>> getPendingProviderRequests(
            @PageableDefault(
                page = 0,
                size = 20,
                sort = "id",
                direction = Sort.Direction.ASC) Pageable pageable
            ) {
        return ResponseEntity.ok(
            new ApiResponse<>(
                userService.getAllProviderRequestNotApproved(pageable), 
                "Fetched pending provider requests successfully")
        );
    }

    /**
     * Approves a provider request by its ID.
     * <p>
     * This endpoint looks up the provider request by ID, approves it, and returns a success message.
     *
     * @param id The ID of the provider request to approve
     * @return ResponseEntity containing a success message
     * @throws UserNotFoundException if the request is not found
     * @throws IllegalStateException if the request is not pending
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/provider-requests/{id}/approve")
    public ResponseEntity<MessageResponse> approveProviderRole(@PathVariable Long id) {
        userService.approveProviderRequest(id);
        return ResponseEntity.ok(
            new MessageResponse("Provider request approved successfully."));
    }

    /**
     * Enables a user account by email.
     * <p>
     * This endpoint looks up the user by email, enables the account, and returns a success message.
     *
     * @param email The email of the user to enable
     * @return ResponseEntity containing a success message
     * @throws UserNotFoundException if the user is not found
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/users/{email}/enable")
    public ResponseEntity<MessageResponse> enableUserAccount(@PathVariable String email) {
        userService.enableAccount(email);
        return ResponseEntity.ok(
            new MessageResponse("User account enabled successfully."));
    }

    /**
     * Disables a user account by email.
     * <p>
     * This endpoint looks up the user by email, disables the account, and returns a success message.
     *
     * @param email The email of the user to disable
     * @return ResponseEntity containing a success message
     * @throws UserNotFoundException if the user is not found
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/users/{email}/disable")
    public ResponseEntity<MessageResponse> disableUserAccount(@PathVariable String email) {
        userService.disableAccount(email);
        return ResponseEntity.ok(
            new MessageResponse("User account disabled successfully."));
    }

    /**
     * Simple record for wrapping message responses in API replies.
     * Used for success and error messages.
     *
     * @param message The message to return
     */
    record MessageResponse(String message) {}

    public record ApiResponse<T>(
        T data,
        String message,
        Instant timestamp
    ) {
        public ApiResponse(T data, String message) {
            this(data, message, Instant.now());
        }
    }
}
