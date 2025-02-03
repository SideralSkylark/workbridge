package com.workbridge.workbridge_app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.workbridge.workbridge_app.dto.UserResponseDTO;
import com.workbridge.workbridge_app.entity.UserRole;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admins")
@RequiredArgsConstructor
public class AdminController {
    
    private final UserService userService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/notAdmin")
    public ResponseEntity<List<UserResponseDTO>> getAllNonAdminUsers() {
        try {
            List<UserResponseDTO> seekers = userService.getUsersByRole(UserRole.SERVICE_SEEKER);
            List<UserResponseDTO> providers = userService.getUsersByRole(UserRole.SERVICE_PROVIDER);

            seekers.addAll(providers);
            return ResponseEntity.ok(seekers);
        } catch (Exception exception) {
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
