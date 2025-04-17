package com.workbridge.workbridge_app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.workbridge.workbridge_app.dto.ProviderRequestDTO;
import com.workbridge.workbridge_app.dto.UserResponseDTO;
import com.workbridge.workbridge_app.entity.ProviderRequest;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admins")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final UserService userService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping()
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        try {
            List<UserResponseDTO> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/nonAdmin")
    public ResponseEntity<List<UserResponseDTO>> getAllNonAdminUsers() {
        try {
            List<UserResponseDTO> nonAdmins = userService.getAllNonAdminUsers();
            return ResponseEntity.ok(nonAdmins);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/provider-requests")
    public ResponseEntity<List<ProviderRequestDTO>> getAllProviderRequestsNotApproved() {
        try {
            List<ProviderRequestDTO> providerRequests = userService.getAllProviderRequestNotApproved();
            return ResponseEntity.ok(providerRequests);
        } catch (Exception exception) {
            log.error("Error fetching provider requests", exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/enable")
    public ResponseEntity<String> enableUserAccount(@RequestParam String email) {
        return updateAccountStatus(email, true);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/disable")
    public ResponseEntity<String> disableUserAccount(@RequestParam String email) {
        return updateAccountStatus(email, false);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/approve-provider/{requestId}")
    public ResponseEntity<?> approveProviderRole(@PathVariable Long requestId) {
        try {
            userService.approveProviderRequest(requestId);

            return ResponseEntity.ok("Provider request approved.");
        } catch (Exception e) {
            log.error("Error approving provider request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while approving the request.");
        }
    }

    private ResponseEntity<String> updateAccountStatus(String email, boolean enable) {
        try {
            boolean statusUpdated = enable ? userService.enableAccount(email) : userService.disableAccount(email);
            if (statusUpdated) {
                return ResponseEntity.ok("User " + (enable ? "enabled" : "disabled") + " successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found or already " + (enable ? "enabled" : "disabled") + ".");
            }
        } catch (UserNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with the specified email does not exist.");
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the account status.");
        }
    }
}
