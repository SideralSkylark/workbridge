package com.workbridge.workbridge_app.image.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.workbridge.workbridge_app.common.response.ErrorResponse;
import com.workbridge.workbridge_app.common.response.ResponseFactory;
import com.workbridge.workbridge_app.image.exception.ImageStorageException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Dedicated exception handler for image-related operations.
 * <p>
 * Intercepts {@link ImageStorageException} thrown by {@link ImageController}
 * and returns a standardized error response format with appropriate status codes.
 *
 * <p>This handler supplements the {@link com.workbridge.workbridge_app.common.exception.GlobalExceptionHandler}
 * by focusing on image-specific domain errors, like invalid uploads or access to non-existent image keys.</p>
 *
 * <p>Typical triggers:</p>
 * <ul>
 *   <li>Empty or invalid image upload</li>
 *   <li>Presigned URL request for a missing image</li>
 *   <li>Unexpected storage failure</li>
 * </ul>
 *
 * @author Workbridge Team
 * @since 2025-07-16
 */
@RestControllerAdvice(assignableTypes = ImageController.class)
@Slf4j
public class ImageExceptionHandler {

    /**
     * Handles domain-specific image storage exceptions.
     * <p>
     * Returns 400 (Bad Request) for user input errors (e.g., empty file),
     * or 500 (Internal Server Error) for unexpected failures during upload or fetch.
     *
     * @param ex      the thrown {@link ImageStorageException}
     * @param request the HTTP request that triggered the error
     * @return a structured {@link ErrorResponse} with context-specific message
     */
    @ExceptionHandler(ImageStorageException.class)
    public ResponseEntity<ErrorResponse> handleImageStorageException(
        ImageStorageException ex,
        HttpServletRequest request
    ) {
        HttpStatus status = determineHttpStatus(ex);
        log.warn("Image error occurred: {} [{} {}]", ex.getMessage(), status.value(), status.getReasonPhrase());

        return ResponseFactory.error(
            status,
            ex.getMessage(),
            request
        );
    }

    /**
     * Maps exception cause or message to a relevant HTTP status code.
     *
     * @param ex the exception to inspect
     * @return {@link HttpStatus#BAD_REQUEST} for client-related errors, {@link HttpStatus#INTERNAL_SERVER_ERROR} otherwise
     */
    private HttpStatus determineHttpStatus(ImageStorageException ex) {
        String message = ex.getMessage().toLowerCase();

        if (message.contains("empty") || message.contains("only image files are allowed") || message.contains("does not exist")) {
            return HttpStatus.BAD_REQUEST;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
