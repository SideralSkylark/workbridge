package com.workbridge.workbridge_app.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.workbridge.workbridge_app.auth.dto.AuthenticationResponseDTO;
import com.workbridge.workbridge_app.auth.dto.EmailVerificationDTO;
import com.workbridge.workbridge_app.auth.dto.LoginRequestDTO;
import com.workbridge.workbridge_app.auth.dto.RegisterRequestDTO;
import com.workbridge.workbridge_app.auth.dto.RegisterResponseDTO;
import com.workbridge.workbridge_app.auth.exception.InvalidCredentialsException;
import com.workbridge.workbridge_app.auth.exception.TokenExpiredException;
import com.workbridge.workbridge_app.auth.exception.TokenVerificationException;
import com.workbridge.workbridge_app.auth.exception.UserAlreadyExistsException;
import com.workbridge.workbridge_app.security.JwtService;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.entity.UserRole;
import com.workbridge.workbridge_app.user.entity.UserRoleEntity;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.repository.UserRepository;
import com.workbridge.workbridge_app.user.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AuthenticationService
 *
 * <p>This service handles core authentication and registration functionality
 * for the WorkBridge application. It is responsible for:
 *
 * <ul>
 *   <li>Registering new users and assigning roles</li>
 *   <li>Sending and verifying email verification codes</li>
 *   <li>Resending verification codes upon user request</li>
 *   <li>Authenticating users and issuing JWT access tokens</li>
 * </ul>
 *
 * <p>It works in collaboration with:
 * <ul>
 *   <li>{@link VerificationService} – for managing email verification tokens</li>
 *   <li>{@link JwtService} – for generating and managing JWT tokens</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final VerificationService verificationService;

   /**
     * Registers a new user account.
     *
     * <p>This method performs the following steps:
     * <ol>
     *   <li>Validates that the username and email are not already taken</li>
     *   <li>Creates a new {@link ApplicationUser} with encoded password and default disabled status</li>
     *   <li>Assigns user roles</li>
     *   <li>Sends a verification email to the user</li>
     * </ol>
     *
     * @param registerRequestDTO DTO containing the user's registration data
     * @return a response containing the user's email
     * @throws UserAlreadyExistsException if the username or email is already in use
     */
    @Transactional
    public RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO) {
        log.info("Attempting to register user with email: {}", registerRequestDTO.getEmail());

        validateRegistrationRequest(registerRequestDTO);

        ApplicationUser user = createUser(registerRequestDTO);
        userRepository.save(user);
        
        verificationService.createAndSendVerificationToken(user);

        log.info("User registered successfully: {}", user.getEmail());
        
        return new RegisterResponseDTO(user.getEmail());
    }

    /**
     * Verifies a user's email using a verification code.
     *
     * <p>If verification is successful, the user's account is enabled
     * and a JWT token is issued.
     *
     * @param emailVerificationDTO DTO containing the email and code to verify
     * @return an {@link AuthenticationResponseDTO} with JWT and user details
     * @throws UserNotFoundException if the user is not found
     * @throws TokenVerificationException if the code is invalid
     * @throws TokenExpiredException if the code has expired
     */
    @Transactional
    public AuthenticationResponseDTO verify(EmailVerificationDTO emailVerificationDTO) {
        log.debug("Verifying email: {}", emailVerificationDTO.getEmail());

        verificationService.verifyToken(emailVerificationDTO.getEmail(), emailVerificationDTO.getCode());

        ApplicationUser user = findUserByEmailOrThrow(emailVerificationDTO.getEmail());

        user.setEnabled(true);
        userRepository.save(user);

        log.info("Email verified successfully for user: {}", user.getEmail());

        String tokenJwt = jwtService.generateToken(user);
        return buildAuthenticationResponse(user, tokenJwt);
    }

    /**
     * Resends the email verification code to a user.
     *
     * <p>If the user is already verified, no new code is sent.
     * Otherwise, a new verification token is generated and emailed.
     *
     * @param email the email address of the user
     * @return a {@link RegisterResponseDTO} containing the user's email
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional
    public RegisterResponseDTO resendVerificationCode(String email) {
        log.debug("Resending verification code to: {}", email);

        ApplicationUser user = findUserByEmailOrThrow(email);

        if (user.isEnabled()) {
            log.info("User already verified: {}", email);
            return new RegisterResponseDTO(user.getEmail());
        }

        verificationService.deleteExistingToken(email);
        verificationService.createAndSendVerificationToken(user);

        log.info("Verification code resent to: {}", email);

        return new RegisterResponseDTO(user.getEmail());
    }

    /**
     * Authenticates a user and returns a JWT token upon successful login.
     *
     * <p>The method checks:
     * <ul>
     *   <li>That the email exists in the system</li>
     *   <li>That the password is correct</li>
     *   <li>That the user account is enabled</li>
     * </ul>
     *
     * @param loginRequestDTO DTO containing the user's email and password
     * @return an {@link AuthenticationResponseDTO} with the JWT token and user details
     * @throws InvalidCredentialsException if credentials are invalid
     * @throws UserNotFoundException if the account is not verified
     */
    @Transactional
    public AuthenticationResponseDTO login(LoginRequestDTO loginRequestDTO) {
        log.info("User attempting login with email: {}", loginRequestDTO.getEmail());

        ApplicationUser user = userRepository.findByEmail(loginRequestDTO.getEmail())
            .orElseThrow(() -> {
                log.warn("Login failed: user not found - {}", loginRequestDTO.getEmail());
                return new InvalidCredentialsException("Invalid credentials");
            });

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid password for email - {}", loginRequestDTO.getEmail());
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (!user.isEnabled()) {
            log.warn("Login failed: account not verified for email - {}", loginRequestDTO.getEmail());
            throw new UserNotFoundException("Account not verified. Please verify your email first.");
        }

        String token = jwtService.generateToken(user);

        log.info("Login successful for user: {}", user.getEmail());

        return buildAuthenticationResponse(user, token);
    }

    /**
     * Validates whether the given username and email are unique.
     *
     * @param request the registration request
     * @throws UserAlreadyExistsException if the username or email is already taken
     */
    private void validateRegistrationRequest(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            log.info("Registration failed: username already taken - {}", request.getUsername());
            throw new UserAlreadyExistsException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.info("Registration failed: email already in use - {}", request.getEmail());
            throw new UserAlreadyExistsException("Email is already in use");
        }
    }

    /**
     * Creates a new {@link ApplicationUser} entity from the registration request.
     *
     * <p>This method sets up all user fields including:
     * <ul>
     *   <li>Username, email, and encoded password</li>
     *   <li>Account status as disabled</li>
     *   <li>Timestamp metadata</li>
     *   <li>User roles</li>
     * </ul>
     *
     * @param request the registration request
     * @return the created {@link ApplicationUser} instance
     * @throws IllegalArgumentException if a provided role is invalid
     */
    private ApplicationUser createUser(RegisterRequestDTO request) {
        log.debug("Creating user with username: {}, roles: {}", request.getUsername(), request.getRoles());
        
        ApplicationUser user = new ApplicationUser();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        Set<UserRoleEntity> roles = resolveRoles(request.getRoles());

        user.setRoles(roles);
        return user;
    }

    /**
     * Resolves a list of role names (as strings) into corresponding {@link UserRoleEntity} objects.
     *
     * <p>This method:
     * <ul>
     *   <li>Converts each role name to uppercase and maps it to the corresponding {@link UserRole} enum</li>
     *   <li>Fetches the matching {@link UserRoleEntity} from the repository</li>
     *   <li>Throws an {@link IllegalArgumentException} if any role is invalid or not found</li>
     * </ul>
     *
     * @param roleNames A set of role names provided by the user (e.g., "user", "service_provider")
     * @return A set of resolved {@link UserRoleEntity} objects
     * @throws IllegalArgumentException if any role name is not valid or not present in the database
     */
    private Set<UserRoleEntity> resolveRoles(List<String> roleNames) {
        return roleNames.stream()
            .map(role -> roleRepository.findByRole(UserRole.valueOf(role.toUpperCase()))
                .orElseThrow(() -> {
                    log.warn("Invalid role provided: {}", role);
                    return new IllegalArgumentException("Invalid role: " + role);
                }))
            .collect(Collectors.toSet());
    }

    /**
     * Retrieves a user by their email or throws a {@link UserNotFoundException} if not found.
     *
     * <p>The email is expected to be already normalized (lowercased and trimmed) by the caller.
     * If the user does not exist, a warning is logged and a {@code UserNotFoundException} is thrown.
     *
     * @param email The normalized email address of the user to retrieve
     * @return The {@link ApplicationUser} corresponding to the given email
     * @throws UserNotFoundException if no user is found with the specified email
     */
    private ApplicationUser findUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("User not found for email: {}", email);
                return new UserNotFoundException("User not found");
            });
    }

    /**
     * Builds an {@link AuthenticationResponseDTO} from the given user and JWT token.
     *
     * @param user the authenticated user
     * @param token the generated JWT token
     * @return a response DTO containing user information and token
     */
    private AuthenticationResponseDTO buildAuthenticationResponse(ApplicationUser user, String token) {
        log.debug("Building authentication response for user: {}", user.getEmail());

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