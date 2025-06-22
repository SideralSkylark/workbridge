package com.workbridge.workbridge_app.user.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.workbridge.workbridge_app.user.dto.ProviderRequestDTO;
import com.workbridge.workbridge_app.user.dto.UserResponseDTO;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.entity.ProviderRequest;

@Component
public class UserMapper {
    public UserResponseDTO toDTO(ApplicationUser user) {
        Set<String> roles = user.getRoles().stream()
                                .map(r -> r.getRole().name())
                                .collect(Collectors.toSet());

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles,
                user.isEnabled());
    }    

    public ProviderRequestDTO toDTO(ProviderRequest request) {
        ApplicationUser u = request.getUser();
        return new ProviderRequestDTO(
                request.getId(),
                u.getUsername(),
                u.getEmail(),
                request.getRequestedOn(),
                request.isApproved(),
                request.getApprovedOn());
    }
}
