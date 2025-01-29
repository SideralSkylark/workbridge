package com.workbridge.workbridge_app.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponseDTO {
    
    private String token;
    private Long id;
    private String username;
    private String email;
    private String role;
    private LocalDateTime updatedAt;  
}