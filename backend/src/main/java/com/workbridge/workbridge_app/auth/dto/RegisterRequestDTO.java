package com.workbridge.workbridge_app.auth.dto;

import lombok.Data;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class RegisterRequestDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @Email 
    @NotBlank(message = "Email is required") 
    private String email;

    @NotBlank(message = "Password is required") 
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters") 
    private String password;

    /**
     * Optional list of role names.
     * - Limit to 3 to prevent abuse
     * - Elements must not be blank
     */
    @Size(min = 1, max = 3)
    private List<@NotBlank(message = "Role must not be blank") String> roles;

    /**
     * Optional user status.
     * Accepts only the canonical literals below.
     * Adjust the pattern as your business rules evolve.
     */
    @Pattern(regexp = "^(ACTIVE|INACTIVE)?$",
             message = "status must be ACTIVE or INACTIVE")
    private String status;
}
