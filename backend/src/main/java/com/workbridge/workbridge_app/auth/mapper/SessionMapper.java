package com.workbridge.workbridge_app.auth.mapper;

import org.springframework.stereotype.Component;

import com.workbridge.workbridge_app.auth.dto.SessionDTO;
import com.workbridge.workbridge_app.auth.entity.RefreshToken;

@Component
public class SessionMapper {
    public SessionDTO toSessionDTO(RefreshToken token) {
        return SessionDTO.builder()
            .tokenId(token.getId())
            .ip(token.getIp())
            .device(token.getUserAgent())
            .loginTime(token.getCreatedAt())
            .active(token.isActive())
            .build();
    }
}
