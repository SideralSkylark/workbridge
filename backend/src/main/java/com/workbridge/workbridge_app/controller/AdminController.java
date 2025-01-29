package com.workbridge.workbridge_app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.workbridge.workbridge_app.dto.UserResponseDTO;
import com.workbridge.workbridge_app.entity.UserRole;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/admins")
@RequiredArgsConstructor
public class AdminController {
    
    private final UserService userService;

    @GetMapping("/notAdmin")
    public ResponseEntity<List<UserResponseDTO>> getAllNonAdminUsers() {
        try {
            List<UserResponseDTO> seekers = userService.getUsersByRole(UserRole.SERVICE_SEEKER);
            List<UserResponseDTO> providers = userService.getUsersByRole(UserRole.SERVICE_PROVIDER);

            seekers.addAll(providers);
            return ResponseEntity.ok(seekers);
        } catch (Exception exception) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/enable")
    public ResponseEntity<String> enableUserAccount(@RequestParam String email) {
        return updateAccountStatus(email, true);
    }

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
                return ResponseEntity.status(400).body("User not found or already " + (enable ? "enabled" : "disabled") + ".");
            }
        } catch (UserNotFoundException exception) {
            return ResponseEntity.status(404).body("User with the specified email does not exist.");
        } catch (Exception exception) {
            return ResponseEntity.status(500).body("An error occurred while updating the account status.");
        }
    }
}
