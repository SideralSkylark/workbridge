package com.workbridge.workbridge_app.common.exception;

import com.workbridge.workbridge_app.auth.exception.UserNotAuthorizedException;
import com.workbridge.workbridge_app.common.response.ErrorResponse;
import com.workbridge.workbridge_app.common.response.ResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * Global exception handler for application-wide concerns.
 *
 * <p>This class intercepts and handles exceptions thrown by any {@code @RestController}
 * in the application. It transforms these exceptions into consistent and meaningful
 * HTTP responses using the {@link ResponseFactory} utility.</p>
 *
 * <p>Handled exception types include:</p>
 * <ul>
 *   <li>Validation errors (400)</li>
 *   <li>Unauthorized and forbidden access (401/403)</li>
 *   <li>Missing endpoints (404)</li>
 *   <li>Unsupported methods (405)</li>
 *   <li>Malformed requests and argument mismatches (400)</li>
 *   <li>Unhandled internal exceptions (500)</li>
 * </ul>
 *
 * <p>All exceptions are logged with appropriate detail to support debugging and observability.</p>
 *
 * @author Workbridge Team
 * @since 2025-07-16
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles validation errors caused by {@code @Valid} annotations in DTOs.
     * Returns all field-level validation messages in a single response.
     *
     * @param ex the validation exception
     * @param request the originating HTTP request
     * @return HTTP 400 Bad Request with field-specific error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        String errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::formatFieldError)
            .collect(Collectors.joining("; "));

        log.warn("Validation failed for {} {}: {}", request.getMethod(), request.getRequestURI(), errors);
        return ResponseFactory.error(HttpStatus.BAD_REQUEST, errors, request);
    }

    /**
     * Handles authorization failures thrown by Spring Security.
     *
     * @param ex the thrown AuthorizationDeniedException
     * @param request the originating HTTP request
     * @return HTTP 403 Forbidden with the exception message
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(
        AuthorizationDeniedException ex,
        HttpServletRequest request) {
        log.warn("Access denied: {} {}", request.getMethod(), request.getRequestURI());
        return ResponseFactory.error(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    /**
     * Handles user-specific authorization failures.
     *
     * @param ex the thrown UserNotAuthorizedException
     * @param request the originating HTTP request
     * @return HTTP 401 Unauthorized with the exception message
     */
    @ExceptionHandler(UserNotAuthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUserNotAuthorized(
        UserNotAuthorizedException ex,
        HttpServletRequest request) {
        log.warn("Unauthorized: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseFactory.error(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    /**
     * Handles requests to undefined routes.
     *
     * @param ex the thrown NoHandlerFoundException
     * @param request the originating HTTP request
     * @return HTTP 404 Not Found with a generic message
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        NoHandlerFoundException ex,
        HttpServletRequest request) {
        log.warn("404 Not Found: {} {}", request.getMethod(), request.getRequestURI());
        return ResponseFactory.error(HttpStatus.NOT_FOUND, "The requested resource was not found", request);
    }

    /**
     * Handles HTTP requests using unsupported methods.
     *
     * @param ex the thrown HttpRequestMethodNotSupportedException
     * @param request the originating HTTP request
     * @return HTTP 405 Method Not Allowed with the exception message
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
        HttpRequestMethodNotSupportedException ex,
        HttpServletRequest request) {
        log.warn("405 Method Not Allowed: {} {}", request.getMethod(), request.getRequestURI());
        return ResponseFactory.error(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request);
    }

    /**
     * Handles malformed or unreadable JSON bodies.
     *
     * @param ex the thrown HttpMessageNotReadableException
     * @param request the originating HTTP request
     * @return HTTP 400 Bad Request with a generic message about malformed body
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(
        HttpMessageNotReadableException ex,
        HttpServletRequest request) {
        log.warn("400 Malformed JSON in request body: {} {}", request.getMethod(), request.getRequestURI());
        return ResponseFactory.error(HttpStatus.BAD_REQUEST, "Malformed JSON or invalid request body", request);
    }

    /**
     * Handles type mismatches in request parameters (e.g., passing a string where a number is expected).
     *
     * @param ex the thrown MethodArgumentTypeMismatchException
     * @param request the originating HTTP request
     * @return HTTP 400 Bad Request with a message about the expected type
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex,
        HttpServletRequest request) {
        String message = String.format("Invalid value for parameter '%s': expected type '%s'",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("400 Type mismatch: {} {} - {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseFactory.error(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Handles illegal arguments passed to controller methods.
     *
     * @param ex the thrown IllegalArgumentException
     * @param request the originating HTTP request
     * @return HTTP 400 Bad Request with the exception message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
        IllegalArgumentException ex,
        HttpServletRequest request) {
        log.warn("400 Illegal argument: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseFactory.error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Fallback handler for all unhandled exceptions.
     * Returns HTTP 500 and logs the full stack trace.
     *
     * @param ex the uncaught exception
     * @param request the originating HTTP request
     * @return HTTP 500 Internal Server Error with a generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex,
        HttpServletRequest request) {
        log.error("500 Internal Server Error on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return ResponseFactory.internalError(ex, request);
    }

    /**
     * Formats a validation error for logging and response messages.
     *
     * @param error the field error
     * @return formatted string message
     */
    private String formatFieldError(FieldError error) {
        return String.format("Field '%s' %s", error.getField(), error.getDefaultMessage());
    }
}
