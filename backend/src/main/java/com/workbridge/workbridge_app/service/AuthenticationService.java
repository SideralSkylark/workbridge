package com.workbridge.workbridge_app.service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.workbridge.workbridge_app.dto.AuthenticationResponseDTO;
import com.workbridge.workbridge_app.dto.LoginRequestDTO;
import com.workbridge.workbridge_app.dto.RegisterRequestDTO;
import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.entity.UserRole;
import com.workbridge.workbridge_app.entity.UserRoleEntity;
import com.workbridge.workbridge_app.exception.UserAlreadyExistsException;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.repository.UserRepository;
import com.workbridge.workbridge_app.repository.UserRoleRepository;
import com.workbridge.workbridge_app.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthenticationResponseDTO register(RegisterRequestDTO registerRequestDTO) {
        if (userRepository.existsByUsername(registerRequestDTO.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken");
        }

        if (userRepository.existsByEmail(registerRequestDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use");
        }

        ApplicationUser user = new ApplicationUser();
        user.setUsername(registerRequestDTO.getUsername());
        user.setEmail(registerRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        Set<UserRoleEntity> roles = registerRequestDTO.getRoles().stream()
                .map(role -> roleRepository.findByRole(UserRole.valueOf(role.toUpperCase()))
                        .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + role)))
                .collect(Collectors.toSet());

        user.setRoles(roles);

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

    private AuthenticationResponseDTO buildAuthenticationResponse(ApplicationUser user, String token) {
        return AuthenticationResponseDTO.builder()
            .token(token)
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .roles(user.getRoles().stream().map(role -> role.getRole().name()).collect(Collectors.toSet()))
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}