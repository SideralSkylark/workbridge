package com.workbridge.workbridge_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    
    private String username;
    private String email;
    private String password;
    private String role;
    private String status;
}
