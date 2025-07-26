package com.workbridge.workbridge_app.auth.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDTO {
    private Long tokenId;
    private String ip;
    private String device;
    private LocalDateTime loginTime;
    private boolean active;
}
