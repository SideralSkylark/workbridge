package com.workbridge.workbridge_app.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.workbridge.workbridge_app.dto.AuthenticationResponseDTO;
import com.workbridge.workbridge_app.dto.LoginRequestDTO;
import com.workbridge.workbridge_app.dto.RegisterRequestDTO;
import com.workbridge.workbridge_app.entity.AccountStatus;
import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.entity.ServiceProvider;
import com.workbridge.workbridge_app.entity.ServiceSeeker;
import com.workbridge.workbridge_app.entity.UserRole;
import com.workbridge.workbridge_app.exception.UserAlreadyExistsException;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.repository.UserRepository;
import com.workbridge.workbridge_app.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationResponseDTO register(RegisterRequestDTO registerRequestDTO) {
        if (userRepository.existsByUsername(registerRequestDTO.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken");
        }

        if (userRepository.existsByEmail(registerRequestDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use");
        }

        ApplicationUser user = createUserFromDTO(registerRequestDTO);
        userRepository.save(user);
        String token = jwtService.generateToken(user);

        return buildAuthenticationResponse(user, token);
    }

    public AuthenticationResponseDTO login(LoginRequestDTO loginRequestDTO) {
        ApplicationUser user = userRepository.findByEmail(loginRequestDTO.getEmail())
            .orElseThrow(() -> new UserNotFoundException("Invalid credentials"));

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            throw new UserNotFoundException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        return buildAuthenticationResponse(user, token);
    }

    private ApplicationUser createUserFromDTO(RegisterRequestDTO dto) {
        UserRole role = UserRole.valueOf(dto.getRole());

        ApplicationUser user;
        switch (role) {
            case SERVICE_PROVIDER:
                user = new ServiceProvider();
                ((ServiceProvider) user).setStatus(AccountStatus.valueOf(dto.getStatus())); 
                break;
            case SERVICE_SEEKER:
                user = new ServiceSeeker();
                break;
            default:
                throw new IllegalArgumentException("Unsupported role: " + role);
        }

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }

    private AuthenticationResponseDTO buildAuthenticationResponse(ApplicationUser user, String token) {
        return AuthenticationResponseDTO.builder()
            .token(token)
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole().name())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}