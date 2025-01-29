package com.workbridge.workbridge_app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/admins")
@RequiredArgsConstructor
public class AdminController {
    
    private final UserService userService;

    //TODO: implement
    @GetMapping("/notAdmin")
    public ResponseEntity<String> getAllNonAdmin() {
        try {
            // two collections for sekker, provider.
            // return custo DTO
            /* 
             *List<CustomUserResponseDTO> seekers = userService.generateCustomUserResponse(userService.findAllUsersByRole(UserRole.SERVICE_SEEKER));
             *List<CustomUserResponseDTO> providers = userService.generateCustomUserResponse(userService.findAllUsersByRole(UserRole.SERVICE_PROVIDERS));
             *return CustomResponseObjectDTO.build(setSeekers(seekers), setProviders(providers));
            */
            
        } catch (Exception exception) {

        }
        return ResponseEntity.ok("");
    }

    @PutMapping("/enable")
    public ResponseEntity<String> enableUserAccount(@RequestParam String email) {
        try {
            boolean isEnabled = userService.enableAccount(email);
            if (isEnabled) {
                return ResponseEntity.ok("Account enabled successfully.");
            } else {
                return ResponseEntity.status(400).body("User not found or already enabled.");
            }
        } catch (UserNotFoundException exception) {
            return ResponseEntity.status(404).body("User with the specified email does not exist.");
        } catch (Exception exception) {
            return ResponseEntity.status(500).body("An error ocured while enabling the account.");
        }
    }

    @PutMapping("/disable")
    public ResponseEntity<String> disableUserAccount(@RequestParam String email) {
        try {
            boolean isDisabled = userService.disableAccount(email);
            if (isDisabled) {
                return ResponseEntity.ok("User disabled successfully.");
            } else {
                return ResponseEntity.status(400).body("User not found or already disabled.");
            }
        } catch (UserNotFoundException exception) {
            return ResponseEntity.status(404).body("User with the specified email does not exist.");
        } catch (Exception exception) {
            return ResponseEntity.status(500).body("An error ocurred while enabling the account.");
        }
    }
}
