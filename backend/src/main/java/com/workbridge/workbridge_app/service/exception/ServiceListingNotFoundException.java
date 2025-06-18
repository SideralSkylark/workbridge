package com.workbridge.workbridge_app.service.exception;

public class ServiceListingNotFoundException extends RuntimeException {
    public ServiceListingNotFoundException(String message) {
        super(message);
    }
}
