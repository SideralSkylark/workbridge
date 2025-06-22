package com.workbridge.workbridge_app.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.workbridge.workbridge_app.user.exception.UserNotFoundException;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for {@link UserController}-related operations.
 * <p>
 * This handler captures and translates specific exceptions into structured
 * HTTP responses with appropriate status codes and messages.
 * <p>
 * It ensures consistent error reporting for client-side handling and improves
 * debuggability through logging.
 * 
 * @author Workbridge Team
 * 
 * @since 2025-06-22
 */
@RestControllerAdvice(assignableTypes = UserController.class)
@Slf4j
public class UserExceptionHandler {

    /**
     * Handles cases where a user is not found in the system.
     *
     * @param ex the thrown {@link UserNotFoundException}
     * @return 404 NOT FOUND with the exception message as response body
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> UserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Handles illegal operations or invalid states, such as:
     * <ul>
     *   <li>Missing authentication context</li>
     *   <li>Invalid request preconditions</li>
     * </ul>
     * <p>
     * Differentiates between authorization failures and bad requests
     * by inspecting the exception message content.
     *
     * @param ex the thrown {@link IllegalStateException}
     * @return 401 UNAUTHORIZED if message relates to authentication,
     *         400 BAD REQUEST otherwise
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> IllegalState(IllegalStateException ex) {
        // Distinguish between auth errors and bad requests via message inspection
        HttpStatus status = ex.getMessage().toLowerCase().contains("authenticated") 
                            ? HttpStatus.UNAUTHORIZED 
                            : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(ex.getMessage());
    }

    /**
     * Handles unexpected, unhandled exceptions.
     * <p>
     * Logs the full stack trace and returns a generic 500 Internal Server Error
     * message to the client, without exposing internal details.
     *
     * @param ex the thrown {@link Exception}
     * @return 500 INTERNAL SERVER ERROR with a generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> Generic(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred. Please try again later.");
    }
}
