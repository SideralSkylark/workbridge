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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for application-wide concerns.
 * <p>
 * This class intercepts exceptions thrown by any {@code @RestController} in the application,
 * ensuring consistent and informative HTTP responses. It leverages the {@link ResponseFactory}
 * utility to wrap error details in a standardized {@link ErrorResponse} format.
 *
 * <p>Currently handled exceptions:</p>
 * <ul>
 *   <li>{@link MethodArgumentNotValidException} – Occurs when {@code @Valid} annotated DTOs fail validation.</li>
 *   <li>{@link Exception} – Fallback handler for all uncaught exceptions.</li>
 * </ul>
 *
 * <p>All handled exceptions are logged using SLF4J to support debugging and monitoring.</p>
 *
 * @author Workbridge Team
 * @since 2025-06-24
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Handles validation errors triggered by {@code @Valid} annotations on request bodies.
     * <p>
     * Collects field-level validation errors and returns them in a readable format with
     * HTTP 400 Bad Request status.
     *
     * @param ex      the validation exception containing binding errors
     * @param request the servlet request from which the error originated
     * @return a structured error response with field-level validation messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validationException(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        String errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::formatFieldError)
            .collect(Collectors.joining("; "));

        log.warn("Validation error: {}", errors);

        return ResponseFactory.error(HttpStatus.BAD_REQUEST, errors, request);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> authorizationDenied(
        AuthorizationDeniedException ex,
        HttpServletRequest request) {
            return ResponseFactory.error(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                request
            );
    }

    @ExceptionHandler(UserNotAuthorizedException.class)
    public ResponseEntity<ErrorResponse> userNotAuthorized(
        UserNotAuthorizedException ex,
        HttpServletRequest request) {
            return ResponseFactory.error(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                request
            );
        }

    /**
     * Handles unexpected uncaught exceptions that fall through other handlers.
     * <p>
     * Logs the exception and returns a generic HTTP 500 Internal Server Error response
     * with a standard message to avoid exposing internal details.
     *
     * @param ex      the uncaught exception
     * @param request the request that triggered the error
     * @return a generic error response with HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> generic(
        Exception ex,
        HttpServletRequest request) {
        log.error("Unexpected error ocurred.", ex);
        return ResponseFactory.internalError(ex, request);
    }

    private String formatFieldError(FieldError error) {
        return String.format("Field '%s' %s", error.getField(), error.getDefaultMessage());
    }
}
