package com.workbridge.workbridge_app.review.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.workbridge.workbridge_app.booking.exception.BookingNotFoundException;
import com.workbridge.workbridge_app.common.response.ErrorResponse;
import com.workbridge.workbridge_app.common.response.ResponseFactory;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception handler for review-related REST endpoints.
 * <p>
 * This class provides centralized exception handling for all endpoints in {@link ReviewController}.
 * It ensures that errors are returned in a consistent format using {@link ErrorResponse} and {@link ResponseFactory}.
 *
 * <p>Handled exceptions:</p>
 * <ul>
 *   <li>{@link UserNotFoundException} - Returns 404 NOT FOUND with a descriptive message</li>
 *   <li>{@link BookingNotFoundException} - Returns 404 NOT FOUND with a descriptive message</li>
 * </ul>
 *
 * <p>All exceptions are logged using SLF4J for audit and debugging purposes.</p>
 *
 * @author Workbridge Team
 * @since 2025-06-25
 */
@RestControllerAdvice(assignableTypes = ReviewController.class)
@Slf4j
public class ReviewExceptionHandler {
    /**
     * Handles cases where a user is not found during review operations.
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
     * Handles cases where a booking is not found during review operations.
     *
     * @param ex      the thrown BookingNotFoundException
     * @param request the HTTP request for extracting the URI
     * @return 404 NOT FOUND with a descriptive error message
     */
    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ErrorResponse> bookingNotFound(
        BookingNotFoundException ex,
        HttpServletRequest request) {
            return ResponseFactory.error(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
            );
        }
}
