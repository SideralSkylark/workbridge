package com.workbridge.workbridge_app.exception;

public class ServiceListingNotFoundException extends RuntimeException {
    public ServiceListingNotFoundException(String message) {
        super(message);
    }
}
