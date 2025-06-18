package com.workbridge.workbridge_app.auth.dto;

import lombok.Data;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class RegisterRequestDTO {
    @NotBlank 
    private String username;

    @Email 
    @NotBlank 
    private String email;

    @NotBlank 
    @Size(min = 8, max = 100) 
    private String password;

    /**
     * Optional list of role names.
     * - Limit to 3 to prevent abuse
     * - Elements must not be blank
     */
    @Size(max = 3)
    private List<@NotBlank String> roles;

    /**
     * Optional user status.
     * Accepts only the canonical literals below.
     * Adjust the pattern as your business rules evolve.
     */
    @Pattern(regexp = "^(ACTIVE|INACTIVE)?$",
             message = "status must be ACTIVE or INACTIVE")
    private String status;
}
