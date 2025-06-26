package com.workbridge.workbridge_app.service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.workbridge.workbridge_app.common.response.ErrorResponse;
import com.workbridge.workbridge_app.common.response.ResponseFactory;
import com.workbridge.workbridge_app.service.exception.ServiceNotFoundException;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.exception.UserNotServiceProviderException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception handler for service-related REST endpoints.
 * <p>
 * This class provides centralized exception handling for all endpoints in {@link ServiceController}.
 * It ensures that errors are returned in a consistent format using {@link ErrorResponse} and {@link ResponseFactory}.
 *
 * <p>Handled exceptions:</p>
 * <ul>
 *   <li>{@link ServiceNotFoundException} - Returns 404 NOT FOUND if a service is not found</li>
 *   <li>{@link UserNotFoundException} - Returns 404 NOT FOUND if a user is not found</li>
 *   <li>{@link UserNotServiceProviderException} - Returns 403 FORBIDDEN if a user is not a service provider</li>
 * </ul>
 *
 * <p>All exceptions are logged using SLF4J for audit and debugging purposes.</p>
 *
 * @author Workbridge Team
 * @since 2025-06-26
 */
@RestControllerAdvice(assignableTypes = ServiceController.class)
@Slf4j
public class ServiceExceptionHandler {
    /**
     * Handles cases where a service is not found.
     *
     * @param ex      the thrown ServiceNotFoundException
     * @param request the HTTP request for extracting the URI
     * @return 404 NOT FOUND with a descriptive error message
     */
    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> serviceNotFound(
        ServiceNotFoundException ex,
        HttpServletRequest request) {
        return ResponseFactory.error(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            request
            );
        }
    
    /**
     * Handles cases where a user is not found during service operations.
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
     * Handles cases where a user is not a service provider.
     *
     * @param ex      the thrown UserNotServiceProviderException
     * @param request the HTTP request for extracting the URI
     * @return 403 FORBIDDEN with a descriptive error message
     */
    @ExceptionHandler(UserNotServiceProviderException.class)
    public ResponseEntity<ErrorResponse> userNotServiceProvider(
        UserNotServiceProviderException ex,
        HttpServletRequest request) {
            return ResponseFactory.error(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                request
            );
        }
}
