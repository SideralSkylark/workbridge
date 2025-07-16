package com.workbridge.workbridge_app.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.workbridge.workbridge_app.common.response.ErrorResponse;
import com.workbridge.workbridge_app.common.response.ResponseFactory;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception handler for administrative operations in the system.
 * <p>
 * This class provides centralized exception handling for all endpoints in {@link AdminController}.
 * It ensures that errors are logged and returned to the client in a consistent format using {@link AdminController.MessageResponse}.
 *
 * <p>Handled exceptions:</p>
 * <ul>
 *   <li>{@link UserNotFoundException} - Returns 404 NOT FOUND with a descriptive message</li>
 *   <li>{@link IllegalStateException} - Returns 400 BAD REQUEST with a descriptive message</li>
 *   <li>{@link Exception} (fallback) - Returns 500 INTERNAL SERVER ERROR with a generic message</li>
 * </ul>
 *
 * <p>All exceptions are logged using SLF4J for audit and debugging purposes.</p>
 *
 * @author Workbridge Team
 * @since 2025-06-22
 */
@RestControllerAdvice(assignableTypes = AdminController.class)
@Slf4j
public class AdminExceptionHandler {
    /**
     * Handles cases where a user or resource is not found.
     *
     * @param ex the thrown UserNotFoundException
     * @return 404 NOT FOUND with a descriptive error message
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> userNotFound(
        UserNotFoundException ex,
        HttpServletRequest request) {
        log.warn("AdminController - User not found on {} {}: {}",
            request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseFactory.error(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            request
        );
    }

    /**
     * Handles illegal or invalid operations (e.g., approving an already approved request).
     *
     * @param ex the thrown IllegalStateException
     * @return 400 BAD REQUEST with a descriptive error message
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> illegalState(
        IllegalStateException ex,
        HttpServletRequest request) {
        HttpStatus status = ex.getMessage().toLowerCase().contains("authenticated")
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.BAD_REQUEST;

        log.warn("AdminController - Illegal state on {} {}: {}",
            request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseFactory.error(
            status,
            ex.getMessage(),
            request
        );
    }
}
