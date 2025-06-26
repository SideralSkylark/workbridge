package com.workbridge.workbridge_app.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {
    public static String getAuthenticatedUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null || auth.getName().isBlank()) {
            throw new IllegalStateException("Authenticated user not found");
        }
        return auth.getName();
    }
}
