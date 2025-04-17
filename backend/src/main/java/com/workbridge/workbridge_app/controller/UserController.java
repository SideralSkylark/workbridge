package com.workbridge.workbridge_app.controller;

import com.workbridge.workbridge_app.dto.UserResponseDTO;
import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

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
        } catch (Exception e) {
            log.error("Error retrieving user details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateUserDetails(@RequestBody UserResponseDTO userResponseDTO) {
        try {
            String username = getAuthenticatedUsername();
            ApplicationUser updatedUser = userService.updateUser(username, userResponseDTO);
            return ResponseEntity.ok(userService.convertToDTO(updatedUser));
        } catch (UserNotFoundException e) {
            log.warn("User update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating user details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser() {
        try {
            String username = getAuthenticatedUsername();
            userService.deleteByUsername(username);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            log.warn("User deletion failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PreAuthorize("hasRole('ROLE_SERVICE_SEEKER')")
    @PostMapping("/me/request-to-become-provider")
    public ResponseEntity<?> requestToBecomeProvider() {
        try {
            String username = getAuthenticatedUsername();
            userService.requestToBecomeProvider(username);

            return ResponseEntity.ok("Request to become a service provider sent.");
        } catch (Exception e) {
            log.error("Error requesting to become provider", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

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
        } catch (Exception e) {
            log.error("Error checking provider request status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get status.");
        }
    }


    private String getAuthenticatedUsername() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Authenticated user not found.");
        }
        return username;
    }
}