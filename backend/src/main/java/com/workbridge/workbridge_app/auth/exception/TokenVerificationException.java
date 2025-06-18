package com.workbridge.workbridge_app.auth.exception;

public class TokenVerificationException extends RuntimeException {
    public TokenVerificationException(String message) {
        super(message);
    }
} 