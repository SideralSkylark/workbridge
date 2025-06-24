package com.workbridge.workbridge_app.common.response;

import java.time.Instant;

public record ApiResponse<T>(
    T data,
    String message,
    Instant timestamp
) {
    public ApiResponse(T data, String message) {
        this(data, message, Instant.now());
    }
}
