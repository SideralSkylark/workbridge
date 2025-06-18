package com.workbridge.workbridge_app.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;

/**
 * Controller responsible for handling authentication-related endpoints.
 * This controller manages user registration, email verification, login, and token-related operations.
 * It provides endpoints for:
 * - User registration
 * - Email verification
 * - Verification code resending
 * - User login
 * 
 * The controller also includes global exception handlers for authentication-related exceptions.
 */
@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    
    /**
     * Registers a new user in the system.
     * This endpoint:
     * 1. Validates the registration request
     * 2. Creates a new user account
     * 3. Sends a verification email
     * 4. Returns a registration response
     *
     * @param registerRequest The registration request containing user details
     * @return RegisterResponseDTO containing the registered user's email
     * @throws UserAlreadyExistsException if username or email is already in use
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody RegisterRequestDTO registerRequest) {
        RegisterResponseDTO response = authenticationService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    @PostMapping("/verify")
    public ResponseEntity<AuthenticationResponseDTO> verify(@RequestBody EmailVerificationDTO emailVerificationDTO) {
        AuthenticationResponseDTO response = authenticationService.verify(emailVerificationDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Resends a verification code to a user's email.
     * If the user is already verified, returns the email without sending a new code.
     * Otherwise, deletes any existing token and sends a new verification code.
     *
     * @param request Contains the email address to resend the verification code to
     * @return RegisterResponseDTO containing the user's email
     * @throws UserNotFoundException if user not found
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<RegisterResponseDTO> resendVerification(@RequestBody EmailVerificationDTO request) {
        RegisterResponseDTO response = authenticationService.resendVerificationCode(request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Authenticates a user and generates a JWT token.
     * This endpoint:
     * 1. Validates user credentials
     * 2. Checks if the account is verified
     * 3. Generates and returns a JWT token
     *
     * @param loginRequest Contains email and password
     * @return AuthenticationResponseDTO with JWT token and user details
     * @throws InvalidCredentialsException if email or password is incorrect
     * @throws UserNotFoundException if account is not verified
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        AuthenticationResponseDTO response = authenticationService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Global exception handler for UserAlreadyExistsException.
     * Returns a 409 Conflict status with the exception message.
     *
     * @param ex The UserAlreadyExistsException that was thrown
     * @return ResponseEntity containing the error message
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    /**
     * Global exception handler for UserNotFoundException.
     * Returns a 404 Not Found status with the exception message.
     *
     * @param ex The UserNotFoundException that was thrown
     * @return ResponseEntity containing the error message
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Global exception handler for InvalidCredentialsException.
     * Returns a 401 Unauthorized status with the exception message.
     *
     * @param ex The InvalidCredentialsException that was thrown
     * @return ResponseEntity containing the error message
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    /**
     * Global exception handler for TokenVerificationException.
     * Returns a 400 Bad Request status with the exception message.
     *
     * @param ex The TokenVerificationException that was thrown
     * @return ResponseEntity containing the error message
     */
    @ExceptionHandler(TokenVerificationException.class)
    public ResponseEntity<String> handleTokenVerificationException(TokenVerificationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Global exception handler for TokenExpiredException.
     * Returns a 400 Bad Request status with the exception message.
     *
     * @param ex The TokenExpiredException that was thrown
     * @return ResponseEntity containing the error message
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<String> handleTokenExpiredException(TokenExpiredException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("An unexpected error occurred. Please try again later.");
    }
}