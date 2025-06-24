package com.workbridge.workbridge_app.common.response;

import java.time.Instant;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
    int status,
    String message,
    String path,
    Instant Timestamp
) {
    public static ErrorResponse of(
        HttpStatus status,
        String message,
        String path
    ) {
        return new ErrorResponse(
            status.value(),
            message,
            path,
            Instant.now());
    }
}
