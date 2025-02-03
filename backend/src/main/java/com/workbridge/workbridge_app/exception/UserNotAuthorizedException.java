package com.workbridge.workbridge_app.exception;

public class UserNotAuthorizedException extends RuntimeException {

    public UserNotAuthorizedException(String message) {
        super(message);
    }
}