package com.workbridge.workbridge_app.auth.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.workbridge.workbridge_app.auth.dto.SessionDTO;
import com.workbridge.workbridge_app.auth.exception.InvalidCredentialsException;
import com.workbridge.workbridge_app.auth.exception.TokenExpiredException;
import com.workbridge.workbridge_app.auth.exception.TokenVerificationException;
import com.workbridge.workbridge_app.auth.exception.UserAlreadyExistsException;
import com.workbridge.workbridge_app.auth.service.AuthenticationService;
import com.workbridge.workbridge_app.common.response.ApiResponse;
import com.workbridge.workbridge_app.common.response.MessageResponse;
import com.workbridge.workbridge_app.common.response.ResponseFactory;
import com.workbridge.workbridge_app.security.SecurityUtil;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * REST controller responsible for user authentication and registration operations.
 *
 * <p>This controller provides endpoints for:</p>
 * <ul>
 *   <li>User registration</li>
 *   <li>Email verification and resending verification codes</li>
 *   <liUser login with JWT issuance</li>
 *   <li>Access token refresh</li>
 *   <li>Logout (current and remote sessions)</li>
 *   <li>Listing active sessions</li>
 * </ul>
 *
 * <p>It delegates authentication logic to {@link AuthenticationService} and returns
 * consistent responses using {@link ApiResponse} and {@link ResponseFactory}.</p>
 *
 * <p>Typical responses:</p>
 * <ul>
 *   <li><b>201 Created</b> – for successful registration</li>
 *   <li><b>200 OK</b> – for successful login, verification, logout, etc.</li>
 *   <li><b>4xx</b> – for user, token, or credential-related errors</li>
 * </ul>
 *
 * <p>All request DTOs are validated using {@code @Valid}. Exception handling is centralized via
 * {@link com.workbridge.workbridge_app.auth.exception} and custom handlers.</p>
 *
 * @author WorkBridge
 * @since 2025-06-22
 */
@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Registers a new user account and sends a verification email.
     *
     * @param registerRequest DTO containing username, email, password, and roles
     * @return 201 Created with the user's email
     * @throws UserAlreadyExistsException if username or email already exists
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
     * Verifies a user’s email address using the verification code sent to their inbox.
     *
     * <p>Also activates the user and returns a JWT token.</p>
     *
     * @param emailVerificationDTO DTO with email and verification code
     * @return 200 OK with authentication response (token and user info)
     * @throws UserNotFoundException if the user doesn't exist
     * @throws TokenVerificationException if token is invalid
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
     * @param email User's email address
     * @return 200 OK confirming the dispatch
     * @throws UserNotFoundException if no user exists for the given email
     */
    @PostMapping("/resend-verification/{email}")
    public ResponseEntity<ApiResponse<RegisterResponseDTO>>
     resendVerification(@PathVariable String email) {
        return ResponseFactory.ok(
            authenticationService.resendVerificationCode(email),
            "Verification code resent successfully."
        );
    }

    /**
     * Logs in a user and returns a JWT access token and refresh token via HTTP-only cookie.
     *
     * <p>Only verified users are allowed to log in.</p>
     *
     * @param loginRequest DTO with email and password
     * @param request HTTP request (used for device/IP tracking)
     * @param response HTTP response (used to attach refresh token cookie)
     * @return 200 OK with access token and user info
     * @throws InvalidCredentialsException if credentials are incorrect
     * @throws UserNotFoundException if the user is not verified
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponseDTO>>
     login(
        @Valid @RequestBody LoginRequestDTO loginRequest,
        HttpServletRequest request,
        HttpServletResponse response) {
        return ResponseFactory.ok(
            authenticationService.login(loginRequest, request,  response),
            "User logged in successfully."
        );
    }

    /**
     * Refreshes the access token using the refresh token stored in the user's cookie.
     *
     * <p>The refreshed token is set in the response header.</p>
     *
     * @param request HTTP request (used to extract refresh token)
     * @param response HTTP response (used to set new access token)
     * @return 200 OK with success message
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<MessageResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.refreshAccessToken(request, response);
        return ResponseFactory.okMessage("Token refreshed successfully");
    }

    /**
     * Logs out the currently authenticated user and invalidates their current session.
     *
     * <p>This removes the refresh token and clears session-related data.</p>
     *
     * @param request HTTP request containing session info
     * @param response HTTP response to clear cookies
     * @return 200 OK
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.logout(request, response);
        return ResponseFactory.ok();
    }

    /**
     * Retrieves all active sessions for the currently logged-in user.
     *
     * <p>Useful for multi-device session management.</p>
     *
     * @param pageable Pagination and sorting parameters (sorted by token ID descending by default)
     * @return 200 OK with a list of session DTOs
     */
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<PagedModel<SessionDTO>>> listSessions(
        @PageableDefault(
            page = 0,
            size = 20,
            sort = "id",
            direction = Sort.Direction.DESC
        )Pageable pageable) {
        return ResponseFactory.ok(
           new PagedModel<>(authenticationService.listSessions(SecurityUtil.getAuthenticatedUsername(), pageable)),
            "Sessions fetched successfully."
        );
    }

    /**
     * Logs out a specific session (remote logout) by its token ID.
     *
     * <p>This allows users to terminate other device sessions remotely.</p>
     *
     * @param tokenId ID of the refresh token/session to revoke
     * @return 200 OK
     */
    @PostMapping("/logout/{tokenId}")
    public ResponseEntity<ApiResponse<Void>> remoteLogout(@PathVariable Long tokenId) {
        authenticationService.logoutWithToken(tokenId);
        return ResponseFactory.ok();
    }
}
