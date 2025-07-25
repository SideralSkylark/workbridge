package com.workbridge.workbridge_app.auth.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionDTO {
    private Long tokenId;
    private String ip;
    private String device;
    private LocalDateTime loginTime;
    private boolean active;
}
