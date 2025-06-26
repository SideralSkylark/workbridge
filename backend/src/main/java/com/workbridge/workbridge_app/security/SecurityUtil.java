package com.workbridge.workbridge_app.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility component for accessing security-related information from the Spring Security context.
 * <p>
 * Provides static helper methods to retrieve details about the currently authenticated user.
 * </p>
 *
 * <p>Typical usage:</p>
 * <pre>
 *   String username = SecurityUtil.getAuthenticatedUsername();
 * </pre>
 *
 * <p>Throws {@link IllegalStateException} if no authenticated user is found in the security context.</p>
 *
 * @author Workbridge Team
 * @since 2025-06-26
 */
@Component
public class SecurityUtil {
    /**
     * Retrieves the username of the currently authenticated user from the security context.
     *
     * @return the username of the authenticated user
     * @throws IllegalStateException if no authenticated user is found or the username is blank
     */
    public static String getAuthenticatedUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null || auth.getName().isBlank()) {
            throw new IllegalStateException("Authenticated user not found");
        }
        return auth.getName();
    }
}
