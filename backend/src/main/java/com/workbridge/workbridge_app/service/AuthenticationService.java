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

/**
 * Service responsible for handling user authentication and registration operations.
 * This service manages user registration,
 * email verification, 
 * login, and token generation.
 * It works in conjunction with the VerificationService for email 
 * verification and JwtService for token management.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final VerificationService verificationService;

    /**
     * Registers a new user in the system.
     * This method:
     * 1. Validates the registration request (username and email uniqueness)
     * 2. Creates a new user with encoded password
     * 3. Sends a verification email
     * 4. Returns a registration response
     *
     * @param registerRequestDTO The registration request containing user details
     * @return RegisterResponseDTO containing the registered user's email
     * @throws UserAlreadyExistsException if username or email is already in use
     */
    @Transactional
    public RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO) {
        validateRegistrationRequest(registerRequestDTO);

        ApplicationUser user = createUser(registerRequestDTO);
        userRepository.save(user);
        
        verificationService.createAndSendVerificationToken(user);
        
        return new RegisterResponseDTO(user.getEmail());
    }

    /**
     * Verifies a user's email using the provided verification code.
     * Upon successful verification:
     * 1. Enables the user account
     * 2. Generates a JWT token
     * 3. Returns authentication response with token
     *
     * @param emailVerificationDTO Contains email and verification code
     * @return AuthenticationResponseDTO with JWT token and user details
     * @throws UserNotFoundException if user not found
     * @throws TokenVerificationException if verification code is invalid
     * @throws TokenExpiredException if verification code has expired
     */
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

    /**
     * Resends a verification code to a user's email.
     * If the user is already verified, returns the email without sending a new code.
     * Otherwise, deletes any existing token and sends a new verification code.
     *
     * @param email The email address to resend the verification code to
     * @return RegisterResponseDTO containing the user's email
     * @throws UserNotFoundException if user not found
     */
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

    /**
     * Authenticates a user and generates a JWT token.
     * This method:
     * 1. Validates user credentials
     * 2. Checks if the account is verified
     * 3. Generates and returns a JWT token
     *
     * @param loginRequestDTO Contains email and password
     * @return AuthenticationResponseDTO with JWT token and user details
     * @throws InvalidCredentialsException if email or password is incorrect
     * @throws UserNotFoundException if account is not verified
     */
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

    /**
     * Validates a registration request by checking for existing username and email.
     *
     * @param request The registration request to validate
     * @throws UserAlreadyExistsException if username or email is already in use
     */
    private void validateRegistrationRequest(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use");
        }
    }

    /**
     * Creates a new ApplicationUser entity from a registration request.
     * Sets up user details including:
     * - Username and email
     * - Encoded password
     * - Account status (disabled by default)
     * - Creation and update timestamps
     * - User roles
     *
     * @param request The registration request containing user details
     * @return A new ApplicationUser entity
     * @throws IllegalArgumentException if an invalid role is specified
     */
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

    /**
     * Builds an authentication response DTO from a user entity and JWT token.
     *
     * @param user The authenticated user
     * @param token The JWT token
     * @return AuthenticationResponseDTO containing user details and token
     */
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