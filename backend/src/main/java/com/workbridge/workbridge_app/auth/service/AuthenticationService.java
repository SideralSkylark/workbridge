package com.workbridge.workbridge_app.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.workbridge.workbridge_app.auth.dto.AuthenticationResponseDTO;
import com.workbridge.workbridge_app.auth.dto.EmailVerificationDTO;
import com.workbridge.workbridge_app.auth.dto.LoginRequestDTO;
import com.workbridge.workbridge_app.auth.dto.RegisterRequestDTO;
import com.workbridge.workbridge_app.auth.dto.RegisterResponseDTO;
import com.workbridge.workbridge_app.auth.dto.SessionDTO;
import com.workbridge.workbridge_app.auth.entity.RefreshToken;
import com.workbridge.workbridge_app.auth.exception.InvalidCredentialsException;
import com.workbridge.workbridge_app.auth.exception.InvalidTokenException;
import com.workbridge.workbridge_app.auth.exception.TokenExpiredException;
import com.workbridge.workbridge_app.auth.exception.TokenVerificationException;
import com.workbridge.workbridge_app.auth.exception.UserAlreadyExistsException;
import com.workbridge.workbridge_app.auth.mapper.SessionMapper;
import com.workbridge.workbridge_app.auth.util.CookieUtil;
import com.workbridge.workbridge_app.auth.util.CookieUtil.TokenType;
import com.workbridge.workbridge_app.security.JwtService;
import com.workbridge.workbridge_app.security.SecurityUtil;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.entity.UserRole;
import com.workbridge.workbridge_app.user.entity.UserRoleEntity;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.repository.UserRepository;
import com.workbridge.workbridge_app.user.repository.UserRoleRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AuthenticationService
 *
 * <p>This service manages the core authentication flow of the WorkBridge application.
 * It provides functionality for:
 *
 * <ul>
 *   <li>User registration and role assignment</li>
 *   <li>Email verification (initial and resend)</li>
 *   <li>User login (authentication) and token issuance</li>
 *   <li>JWT refresh token lifecycle handling</li>
 *   <li>User logout and session revocation</li>
 *   <li>Session listing and targeted session invalidation</li>
 * </ul>
 *
 * <p>Collaborates with:
 * <ul>
 *   <li>{@link VerificationService} – for email verification logic</li>
 *   <li>{@link JwtService} – for generating access tokens</li>
 *   <li>{@link RefreshTokenService} – for refresh token lifecycle management</li>
 *   <li>{@link CookieUtil} – for secure cookie handling</li>
 *   <li>{@link SessionMapper} – for transforming session entities to DTOs</li>
 * </ul>
 *
 * @author WorkBridge
 * @since 2025-06-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final String ACCESS_COOKIE = CookieUtil.ACCESS_TOKEN_COOKIE;
    private static final String REFRESH_COOKIE = CookieUtil.REFRESH_TOKEN_COOKIE;

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final VerificationService verificationService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;
    private final SessionMapper sessionMapper;

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
        log.debug("Attempting to register user with email: {}", registerRequestDTO.getEmail());

        validateUniqueUser(registerRequestDTO);

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
        return buildAuthenticationResponse(user);
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
     * @param request to extract client data to be persisted
     * @param response to bind the generated tokens to a cookie
     * @return an {@link AuthenticationResponseDTO} with the  user details
     * @throws InvalidCredentialsException if credentials are invalid
     * @throws UserNotFoundException if the account is not verified
     */
    @Transactional
    public AuthenticationResponseDTO login(
        LoginRequestDTO loginRequestDTO,
        HttpServletRequest request,
        HttpServletResponse response) {
        log.debug("User attempting login with email: {}", loginRequestDTO.getEmail());

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

        issueTokens(user, response, request);

        log.info("Login successful for user: {}", user.getId());
        return buildAuthenticationResponse(user);
    }

    /**
     * Refreshes the user's access token using a valid refresh token from the HTTP cookie.
     *
     * <p>If the refresh token is valid and not expired, new tokens (access and refresh)
     * are issued and stored in cookies.
     *
     * @param request the HTTP request containing the refresh token cookie
     * @param response the HTTP response where new cookies will be set
     * @throws InvalidTokenException if the refresh token is missing, invalid, or expired
     */
    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String token = cookieUtil.extractTokenFromCookie(request, REFRESH_COOKIE);

        if (token == null || !refreshTokenService.isTokenValid(token)) {
            log.warn("Invalid refresh token in refresh request");
            cookieUtil.clearCookie(response, ACCESS_COOKIE);
            cookieUtil.clearCookie(response, REFRESH_COOKIE);
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        ApplicationUser user = refreshTokenService.getUserFromToken(token);
        issueTokens(user, response, request);

        log.info("Refreshed tokens for user: {}", user.getId());
    }

    /**
     * Logs out the current user by revoking their refresh token and clearing authentication cookies.
     *
     * @param request the HTTP request containing the refresh token cookie
     * @param response the HTTP response used to clear the cookies
     */
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.extractTokenFromCookie(request, REFRESH_COOKIE);
        if (refreshToken != null) {
            refreshTokenService.deleteByToken(refreshToken);
        }
        cookieUtil.clearCookie(response, CookieUtil.ACCESS_TOKEN_COOKIE);
        cookieUtil.clearCookie(response, CookieUtil.REFRESH_TOKEN_COOKIE);
        log.info("User logged out.");
    }

    /**
     * Lists all active sessions for the specified user.
     *
     * <p>Returns paginated session data using tokens tied to the user.
     *
     * @param username the username of the user
     * @param pageable pagination information
     * @return a page of {@link SessionDTO} representing active sessions
     * @throws UserNotFoundException if the user is not found
     */
    public Page<SessionDTO> listSessions(String username, Pageable pageable) {
        ApplicationUser user = findUserByUsernameOrThrow(username);
        return refreshTokenService.findAllByUserId(user.getId(), pageable)
            .map(sessionMapper::toSessionDTO);
    }

    /**
     * Logs out (invalidates) a single session using the token ID.
     *
     * <p>Only allows the authenticated user to invalidate their own session token.
     *
     * @param tokenId the ID of the refresh token to invalidate
     * @throws InvalidTokenException if the token does not exist or doesn't belong to the user
     */
    @Transactional
    public void logoutWithToken(Long tokenId) {
        RefreshToken token = refreshTokenService.findByTokenId(tokenId)
            .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        String currentUsername = SecurityUtil.getAuthenticatedUsername();
        if (!token.getUser().getUsername().equals(currentUsername)) {
            log.error("Unauthorized session invalidation attempt by: {}", currentUsername);
            throw new InvalidTokenException("You are not authorized to invalidate this session.");
        }

        refreshTokenService.deleteByToken(token.getToken());
        log.info("Session invalidated for token: {}", tokenId);
    }


    /**
     * Validates whether the given username and email are unique.
     *
     * @param request the registration request
     * @throws UserAlreadyExistsException if the username or email is already taken
     */
    private void validateUniqueUser(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Username taken: {}", request.getUsername());
            throw new UserAlreadyExistsException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email in use: {}", request.getEmail());
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

        Set<UserRoleEntity> roles = resolveRoles(request.getRoles());

        return ApplicationUser.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .enabled(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .roles(roles)
            .build();
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
     * Issues new access and refresh tokens for the given user and stores them in cookies.
     *
     * <p>Uses {@link JwtService} to generate the access token and
     * {@link RefreshTokenService} for refresh token generation.
     *
     * @param user the authenticated user
     * @param response the HTTP response used to set cookies
     * @param request the HTTP request used for IP and user agent info
     */
    private void issueTokens(ApplicationUser user, HttpServletResponse response, HttpServletRequest request) {
        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user, request).getToken();

        cookieUtil.setTokenCookie(response, ACCESS_COOKIE , accessToken, TokenType.ACCESS);
        cookieUtil.setTokenCookie(response, REFRESH_COOKIE, refreshToken, TokenType.REFRESH);
    }

    /**
     * Builds an {@link AuthenticationResponseDTO} from the given user and JWT token.
     *
     * @param user the authenticated user
     * @param token the generated JWT token
     * @return a response DTO containing user information and token
     */
    private AuthenticationResponseDTO buildAuthenticationResponse(ApplicationUser user) {
        log.debug("Building authentication response for user: {}", user.getEmail());

        return AuthenticationResponseDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .roles(user.getRoles().stream()
                .map(role -> role.getRole().name())
                .collect(Collectors.toSet()))
            .updatedAt(user.getUpdatedAt())
            .build();
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
     * Retrieves a user by their username or throws a {@link UserNotFoundException} if not found.
     *
     * <p>The username is expected to be already normalized (lowercased and trimmed) by the caller.
     * If the user does not exist, a warning is logged and a {@code UserNotFoundException} is thrown.
     *
     * @param username The normalized username  of the user to retrieve
     * @return The {@link ApplicationUser} corresponding to the given username
     * @throws UserNotFoundException if no user is found with the specified username
     */
    private ApplicationUser findUserByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> {
                log.warn("User not found for username: {}", username);
                return new UserNotFoundException("User not found");
            });
    }
}
