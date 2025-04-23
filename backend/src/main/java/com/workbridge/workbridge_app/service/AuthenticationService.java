package com.workbridge.workbridge_app.service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.workbridge.workbridge_app.dto.AuthenticationResponseDTO;
import com.workbridge.workbridge_app.dto.EmailVerificationDTO;
import com.workbridge.workbridge_app.dto.LoginRequestDTO;
import com.workbridge.workbridge_app.dto.RegisterRequestDTO;
import com.workbridge.workbridge_app.dto.RegisterResponseDTO;
import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.entity.UserRole;
import com.workbridge.workbridge_app.entity.UserRoleEntity;
import com.workbridge.workbridge_app.exception.InvalidCredentialsException;
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
    private final VerificationService verificationService;

    @Transactional
    public RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO) {
        validateRegistrationRequest(registerRequestDTO);

        ApplicationUser user = createUser(registerRequestDTO);
        userRepository.save(user);
        
        verificationService.createAndSendVerificationToken(user);
        
        return new RegisterResponseDTO(user.getEmail());
    }

    @Transactional
    public AuthenticationResponseDTO verify(EmailVerificationDTO emailVerificationDTO) {
        verificationService.verifyToken(emailVerificationDTO.getEmail(), emailVerificationDTO.getCode());

        ApplicationUser user = userRepository.findByEmail(emailVerificationDTO.getEmail())
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);

        String tokenJwt = jwtService.generateToken(user);
        return buildAuthenticationResponse(user, tokenJwt);
    }

    @Transactional
    public RegisterResponseDTO resendVerificationCode(String email) {
        ApplicationUser user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.isEnabled()) {
            return new RegisterResponseDTO(user.getEmail());
        }

        verificationService.deleteExistingToken(email);
        verificationService.createAndSendVerificationToken(user);

        return new RegisterResponseDTO(user.getEmail());
    }

    @Transactional
    public AuthenticationResponseDTO login(LoginRequestDTO loginRequestDTO) {
        ApplicationUser user = userRepository.findByEmail(loginRequestDTO.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (!user.isEnabled()) {
            throw new UserNotFoundException("Account not verified. Please verify your email first.");
        }

        String token = jwtService.generateToken(user);
        return buildAuthenticationResponse(user, token);
    }

    private void validateRegistrationRequest(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use");
        }
    }

    private ApplicationUser createUser(RegisterRequestDTO request) {
        ApplicationUser user = new ApplicationUser();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        Set<UserRoleEntity> roles = request.getRoles().stream()
                .map(role -> roleRepository.findByRole(UserRole.valueOf(role.toUpperCase()))
                        .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + role)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        return user;
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