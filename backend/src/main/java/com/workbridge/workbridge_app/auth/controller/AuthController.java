package com.workbridge.workbridge_app.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.workbridge.workbridge_app.auth.dto.AuthenticationResponseDTO;
import com.workbridge.workbridge_app.auth.dto.EmailVerificationDTO;
import com.workbridge.workbridge_app.auth.dto.LoginRequestDTO;
import com.workbridge.workbridge_app.auth.dto.RegisterRequestDTO;
import com.workbridge.workbridge_app.auth.dto.RegisterResponseDTO;
import com.workbridge.workbridge_app.auth.exception.InvalidCredentialsException;
import com.workbridge.workbridge_app.auth.exception.TokenExpiredException;
import com.workbridge.workbridge_app.auth.exception.TokenVerificationException;
import com.workbridge.workbridge_app.auth.exception.UserAlreadyExistsException;
import com.workbridge.workbridge_app.auth.service.AuthenticationService;
import com.workbridge.workbridge_app.common.response.ApiResponse;
import com.workbridge.workbridge_app.common.response.ResponseFactory;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;

import jakarta.validation.Valid;

/**
 * REST controller responsible for user authentication and registration endpoints.
 * <p>
 * This controller provides endpoints for:
 * <ul>
 *   <li>User registration</li>
 *   <li>Email verification</li>
 *   <li>Resending verification codes</li>
 *   <li>User login and JWT token generation</li>
 * </ul>
 *
 * <p>Each endpoint delegates to the {@link AuthenticationService} for business logic
 * and uses DTOs to exchange data between the frontend and backend.</p>
 *
 * <p>All endpoints return appropriate HTTP status codes and standardized API responses using {@link ApiResponse} and {@link ResponseFactory}:</p>
 * <ul>
 *   <li><b>201 Created</b> for successful registration</li>
 *   <li><b>200 OK</b> for successful login and verification operations</li>
 *   <li><b>4xx</b> codes for validation, authentication, and business errors</li>
 * </ul>
 *
 * <p>Validation is enforced using {@code @Valid} and specific exceptions such as:</p>
 * <ul>
 *   <li>{@link UserAlreadyExistsException}</li>
 *   <li>{@link UserNotFoundException}</li>
 *   <li>{@link InvalidCredentialsException}</li>
 *   <li>{@link TokenVerificationException}</li>
 *   <li>{@link TokenExpiredException}</li>
 * </ul>
 *
 * <p>Typical usage:</p>
 * <pre>
 *   POST /api/v1/auth/register            // Register a new user
 *   POST /api/v1/auth/verify              // Verify email with code
 *   POST /api/v1/auth/resend-verification // Resend verification code
 *   POST /api/v1/auth/login               // Login and get JWT
 * </pre>
 *
 * @author Workbridge Team
 * @since 2025-06-22
 */
@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Registers a new user account.
     *
     * <p>This endpoint performs:
     * <ol>
     *   <li>Validation of the request payload</li>
     *   <li>Uniqueness check on username and email</li>
     *   <li>User creation and persistence</li>
     *   <li>Email verification token generation and dispatch</li>
     * </ol>
     *
     * @param registerRequest DTO with username, email, password and roles
     * @return 201 Created with user's email indicating successful registration
     * @throws UserAlreadyExistsException if the email or username is already in use
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponseDTO>>
     register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        return ResponseFactory.created(
            authenticationService.register(registerRequest),
            "User registered successfully."
        );
    }

    /**
     * Verifies a user's email using a verification code.
     *
     * <p>This endpoint:
     * <ul>
     *   <li>Validates the verification code</li>
     *   <li>Activates the user account</li>
     *   <li>Generates a JWT token</li>
     * </ul>
     *
     * @param emailVerificationDTO DTO containing email and verification code
     * @return 200 OK with JWT token and basic user information
     * @throws UserNotFoundException if no user is found with the given email
     * @throws TokenVerificationException if the token is invalid
     * @throws TokenExpiredException if the token has expired
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<AuthenticationResponseDTO>>
     verify(@Valid @RequestBody EmailVerificationDTO emailVerificationDTO) {
        return ResponseFactory.ok(
            authenticationService.verify(emailVerificationDTO),
            "Email verified successfully."
        );
    }

    /**
     * Resends a new verification code to the specified email address.
     *
     * <p>If the user is already verified, no new code is sent, and the response returns immediately.
     * Otherwise:
     * <ul>
     *   <li>Any existing tokens are deleted</li>
     *   <li>A new token is generated and sent</li>
     * </ul>
     *
     * @param request DTO containing the user's email
     * @return 200 OK with the user's email confirming dispatch
     * @throws UserNotFoundException if the email is not registered
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<RegisterResponseDTO>>
     resendVerification(@Valid @RequestBody EmailVerificationDTO request) {
        return ResponseFactory.ok(
            authenticationService.resendVerificationCode(request.getEmail()),
            "Verification code resent successfully."
        );
    }

    /**
     * Authenticates a user and returns a JWT token upon successful login.
     *
     * <p>This endpoint:
     * <ol>
     *   <li>Validates the provided email and password</li>
     *   <li>Checks if the user has verified their email</li>
     *   <li>Returns a signed JWT token along with user metadata</li>
     * </ol>
     *
     * @param loginRequest DTO with user's email and password
     * @return 200 OK with authentication response (token and user info)
     * @throws InvalidCredentialsException if email or password is incorrect
     * @throws UserNotFoundException if the user is not verified
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponseDTO>>
     login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        return ResponseFactory.ok(
            authenticationService.login(loginRequest),
            "User logged in successfully."
        );
    }
}
