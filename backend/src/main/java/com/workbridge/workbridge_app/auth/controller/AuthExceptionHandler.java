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
 * Centralised exception mapping for all auth endpoints.
 * 
 * @author Workbridge Team
 * 
 * @since 2025-06-22
 */
@RestControllerAdvice(assignableTypes = AuthController.class)
@Slf4j
public class AuthExceptionHandler {
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
