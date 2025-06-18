package com.workbridge.workbridge_app.user;

import java.time.LocalDateTime;

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
