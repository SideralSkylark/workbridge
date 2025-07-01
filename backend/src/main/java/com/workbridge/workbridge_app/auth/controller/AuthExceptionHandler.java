package com.workbridge.workbridge_app.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.workbridge.workbridge_app.auth.exception.InvalidCredentialsException;
import com.workbridge.workbridge_app.auth.exception.TokenExpiredException;
import com.workbridge.workbridge_app.auth.exception.TokenVerificationException;
import com.workbridge.workbridge_app.auth.exception.UserAlreadyExistsException;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.common.response.ErrorResponse;
import com.workbridge.workbridge_app.common.response.ResponseFactory;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception handler for authentication-related REST endpoints.
 * <p>
 * This class provides centralized exception handling for all endpoints in {@link AuthController}.
 * It ensures that errors are returned in a consistent format using {@link ErrorResponse} and {@link ResponseFactory}.
 *
 * <p>Handled exceptions:</p>
 * <ul>
 *   <li>{@link UserAlreadyExistsException} - Returns 409 CONFLICT if a user already exists</li>
 *   <li>{@link UserNotFoundException} - Returns 404 NOT FOUND if a user is not found</li>
 *   <li>{@link InvalidCredentialsException} - Returns 401 UNAUTHORIZED for invalid login</li>
 *   <li>{@link TokenVerificationException}, {@link TokenExpiredException} - Returns 400 BAD REQUEST for token issues</li>
 *   <li>{@link IllegalArgumentException} - Returns 400 BAD REQUEST for invalid arguments</li>
 * </ul>
 *
 * <p>All exceptions are logged using SLF4J for audit and debugging purposes.</p>
 *
 * @author Workbridge Team
 * @since 2025-06-22
 */
@RestControllerAdvice(assignableTypes = AuthController.class)
@Slf4j
public class AuthExceptionHandler {
    /**
     * Handles cases where a user already exists during registration.
     *
     * @param ex      the thrown UserAlreadyExistsException
     * @param request the HTTP request for extracting the URI
     * @return 409 CONFLICT with a descriptive error message
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> userAlreadyExists(
        UserAlreadyExistsException ex,
        HttpServletRequest request) {
        return ResponseFactory.error(
            HttpStatus.CONFLICT,
            ex.getMessage(),
            request
        );
    }

    /**
     * Handles cases where a user is not found during authentication or verification.
     *
     * @param ex      the thrown UserNotFoundException
     * @param request the HTTP request for extracting the URI
     * @return 404 NOT FOUND with a descriptive error message
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> userNotFound(
        UserNotFoundException ex,
        HttpServletRequest request) {
        return ResponseFactory.error(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            request
        );
    }

    /**
     * Handles invalid login credentials.
     *
     * @param ex      the thrown InvalidCredentialsException
     * @param request the HTTP request for extracting the URI
     * @return 401 UNAUTHORIZED with a descriptive error message
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> invalidCredentials(
        InvalidCredentialsException ex,
        HttpServletRequest request) {
        return ResponseFactory.error(
            HttpStatus.UNAUTHORIZED,
            ex.getMessage(),
            request
        );
    }

    /**
     * Handles token verification and expiration errors.
     *
     * @param ex      the thrown TokenVerificationException or TokenExpiredException
     * @param request the HTTP request for extracting the URI
     * @return 400 BAD REQUEST with a descriptive error message
     */
    @ExceptionHandler({TokenVerificationException.class, TokenExpiredException.class})
    public ResponseEntity<ErrorResponse> tokenError(
        RuntimeException ex,
        HttpServletRequest request) {
        return ResponseFactory.error(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            request
        );
    }

    /**
     * Handles illegal argument errors in authentication endpoints.
     *
     * @param ex      the thrown IllegalArgumentException
     * @param request the HTTP request for extracting the URI
     * @return 400 BAD REQUEST with a descriptive error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> illegalArgument(
        IllegalArgumentException ex,
        HttpServletRequest request) {
        return ResponseFactory.error(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            request
        );
    }
}
