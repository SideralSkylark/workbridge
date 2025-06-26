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

@RestControllerAdvice(assignableTypes = ServiceController.class)
@Slf4j
public class ServiceExceptionHandler {
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
