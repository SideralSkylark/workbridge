package com.workbridge.workbridge_app.auth.dto;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponseDTO {
    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
    private LocalDateTime updatedAt;
}
