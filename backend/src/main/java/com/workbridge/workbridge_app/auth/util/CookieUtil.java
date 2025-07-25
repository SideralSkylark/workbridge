package com.workbridge.workbridge_app.auth.util;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtil {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private static final int ACCESS_TOKEN_MAX_AGE = (int) Duration.ofMinutes(15).getSeconds();
    private static final int REFRESH_TOKEN_MAX_AGE = (int) Duration.ofDays(7).getSeconds();

    @Value("${auth.cookie.secure}")
    private boolean isSecureCookie;

    public enum TokenType {
        ACCESS(ACCESS_TOKEN_MAX_AGE),
        REFRESH(REFRESH_TOKEN_MAX_AGE);

        private final int maxAge;

        TokenType(int maxAge) {
            this.maxAge = maxAge;
        }

        public int getMaxAge() {
            return maxAge;
        }
    }

    /**
     * Sets a secure, HttpOnly cookie for storing tokens.
     *
     * @param response HTTP response to attach the cookie to
     * @param name     name of the cookie
     * @param value    JWT token value
     * @param type     Token type (ACCESS or REFRESH) to determine expiration
     */
    public void setTokenCookie(HttpServletResponse response, String name, String value, TokenType type) {
        Cookie cookie = buildCookie(name, value, type.getMaxAge());
        response.addCookie(cookie);
    }

    /**
     * Clears a cookie (e.g., on logout) by setting its max age to 0.
     *
     * @param response HTTP response
     * @param name     name of the cookie to clear
     */
    public void clearCookie(HttpServletResponse response, String name) {
        Cookie expiredCookie = buildCookie(name, null, 0);
        response.addCookie(expiredCookie);
    }

    /**
     * Extracts the value of a specific cookie from the request.
     *
     * @param request HTTP request
     * @param name    cookie name
     * @return token value, or null if not present
     */
    public String extractTokenFromCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;

        return Optional.ofNullable(request.getCookies())
            .flatMap(cookies ->
                java.util.Arrays.stream(cookies)
                    .filter(cookie -> name.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
            ).orElse(null);
    }

    /**
     * Creates a base cookie with common security attributes.
     */
    private Cookie buildCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(isSecureCookie);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}
