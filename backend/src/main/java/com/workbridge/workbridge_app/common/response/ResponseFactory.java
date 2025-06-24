package com.workbridge.workbridge_app.common.response;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

/**
 * Utility class for constructing standardized HTTP responses used across the application.
 * <p>
 * The {@code ResponseFactory} provides convenient methods for returning success and error responses
 * wrapped in custom DTOs such as {@link ApiResponse}, {@link MessageResponse}, and {@link ErrorResponse}.
 * This promotes consistent response formatting, reduces boilerplate, and improves maintainability.
 * </p>
 *
 * <p><strong>Typical Usage:</strong></p>
 * <pre>{@code
 * return ResponseFactory.ok(userDTO, "User retrieved successfully");
 * return ResponseFactory.error(HttpStatus.NOT_FOUND, "User not found", request);
 * }</pre>
 */
public class ResponseFactory {

    /** 
     * Returns 200 OK with a wrapped payload and custom message. 
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(new ApiResponse<>(data, message, Instant.now()));
    }

    /**
     * Returns 200 OK with a wrapped payload and a default success message.
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ok(data, "Operation successful");
    }

    /**
     * Returns 200 OK with no payload and a default success message.
     */
    public static ResponseEntity<ApiResponse<Void>> ok() {
        return ok(null, "Operation successful");
    }

    /**
     * Returns 201 Created with a wrapped payload and custom message.
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(data, message, Instant.now()));
    }

    /**
     * Returns 201 Created with a wrapped payload and a default message.
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return created(data, "Resource created");
    }

    /**
     * Returns 201 Created with no payload and a default message.
     */
    public static ResponseEntity<ApiResponse<Void>> created() {
        return created(null, "Resource created");
    }

    /**
     * Returns 200 OK with a simple message (no payload).
     */
    public static ResponseEntity<MessageResponse> okMessage(String message) {
        return ResponseEntity.ok(new MessageResponse(message));
    }

    /**
     * Returns 201 Created with a simple message (no payload).
     */
    public static ResponseEntity<MessageResponse> createdMessage(String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse(message));
    }

    /**
     * Returns an error response with the specified status, message, and request URI.
     *
     * @param status  the HTTP status code to return
     * @param message the error message to include in the response
     * @param request the servlet request from which to extract the URI
     */
    public static ResponseEntity<ErrorResponse> error(
            HttpStatus status, String message, HttpServletRequest request
    ) {
        String path = request != null ? request.getRequestURI() : "N/A";
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status, message, path));
    }

    /**
     * Returns an error response with the specified status, message, and path string.
     *
     * @param status  the HTTP status code
     * @param message the error message
     * @param path    the request path to include in the response
     */
    public static ResponseEntity<ErrorResponse> error(
            HttpStatus status, String message, String path
    ) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status, message, path));
    }

    /**
     * Returns a generic 500 Internal Server Error with a fallback message and request URI.
     *
     * @param ex      the exception that was caught
     * @param request the servlet request for path extraction
     */
    public static ResponseEntity<ErrorResponse> internalError(
            Exception ex, HttpServletRequest request
    ) {
        String path = request != null ? request.getRequestURI() : "N/A";
        return internalError(path);
    }

    /**
     * Returns a generic 500 Internal Server Error with a fallback message and explicit path.
     *
     * @param path the request path that caused the error
     */
    public static ResponseEntity<ErrorResponse> internalError(String path) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred. Please try again later.",
                        path
                ));
    }
}
