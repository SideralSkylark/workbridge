package com.workbridge.workbridge_app.dto;

import java.time.LocalDateTime;

import com.workbridge.workbridge_app.entity.ApplicationUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProviderRequestDTO {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime requestedOn;
    private boolean approved;
    private LocalDateTime approvedOn;
}
