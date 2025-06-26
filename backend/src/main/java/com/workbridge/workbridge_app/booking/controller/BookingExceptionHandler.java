package com.workbridge.workbridge_app.booking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.workbridge.workbridge_app.booking.exception.BookingNotFoundException;
import com.workbridge.workbridge_app.common.response.ErrorResponse;
import com.workbridge.workbridge_app.common.response.ResponseFactory;
import com.workbridge.workbridge_app.service.exception.ServiceNotFoundException;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception handler for booking-related REST API errors.
 * <p>
 * This handler intercepts exceptions thrown by {@link BookingController} and returns standardized
 * error responses using {@link ResponseFactory}. It provides specific handling for user, service,
 * and booking not found scenarios, mapping them to HTTP 404 responses with clear error messages.
 * </p>
 *
 * <p>All error responses are wrapped in {@link ErrorResponse} and include the HTTP status, message,
 * and request details for client-side clarity and debugging.</p>
 *
 * @author Sidik
 * @since 2025-06-26
 */
@RestControllerAdvice(assignableTypes = BookingController.class)
@Slf4j
public class BookingExceptionHandler {
    /**
     * Handles cases where the user is not found.
     *
     * @param ex the thrown UserNotFoundException
     * @param request the HTTP request
     * @return a standardized 404 error response
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
     * Handles cases where the service is not found.
     *
     * @param ex the thrown ServiceNotFoundException
     * @param request the HTTP request
     * @return a standardized 404 error response
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
     * Handles cases where the booking is not found.
     *
     * @param ex the thrown BookingNotFoundException
     * @param request the HTTP request
     * @return a standardized 404 error response
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
