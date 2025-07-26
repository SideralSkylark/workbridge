package com.workbridge.workbridge_app.auth.util;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Utility class for managing secure HTTP cookies related to authentication tokens.
 *
 * <p>This class provides helper methods for:</p>
 * <ul>
 *   <li>Setting HttpOnly cookies for access and refresh tokens</li>
 *   <li>Clearing cookies on logout</li>
 *   <li>Extracting token values from request cookies</li>
 * </ul>
 *
 * <p>All cookies are configured with security best practices, such as:</p>
 * <ul>
 *   <li><b>HttpOnly</b> – prevents JavaScript access</li>
 *   <li><b>Secure</b> – set based on application property ({@code auth.cookie.secure})</li>
 *   <li><b>Path</b> – defaults to root ("/")</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 *   cookieUtil.setTokenCookie(response, CookieUtil.ACCESS_TOKEN_COOKIE, jwt, TokenType.ACCESS);
 *   cookieUtil.clearCookie(response, CookieUtil.REFRESH_TOKEN_COOKIE);
 *   String token = cookieUtil.extractTokenFromCookie(request, CookieUtil.ACCESS_TOKEN_COOKIE);
 * </pre>
 *
 * @author WorkBridge
 * @since 2025-06-22
 */
@Component
public class CookieUtil {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private static final int ACCESS_TOKEN_MAX_AGE = (int) Duration.ofMinutes(15).getSeconds();
    private static final int REFRESH_TOKEN_MAX_AGE = (int) Duration.ofDays(7).getSeconds();

    @Value("${auth.cookie.secure}")
    private boolean isSecureCookie;

    /**
     * Enum representing the types of tokens and their associated expiration times (in seconds).
     */
    public enum TokenType {
        ACCESS(ACCESS_TOKEN_MAX_AGE),
        REFRESH(REFRESH_TOKEN_MAX_AGE);

        private final int maxAge;

        TokenType(int maxAge) {
            this.maxAge = maxAge;
        }

        /**
         * Returns the expiration time in seconds.
         *
         * @return the max age of the token
         */
        public int getMaxAge() {
            return maxAge;
        }
    }

    /**
     * Sets a secure, HttpOnly cookie for the given token.
     *
     * <p>The cookie will be configured with attributes:</p>
     * <ul>
     *   <li>{@code HttpOnly = true}</li>
     *   <li>{@code Secure = true/false} based on application config</li>
     *   <li>{@code Max-Age} based on the token type</li>
     *   <li>{@code Path = "/"}</li>
     * </ul>
     *
     * @param response HTTP response to which the cookie will be added
     * @param name     Name of the cookie (e.g., access_token, refresh_token)
     * @param value    JWT token value to store
     * @param type     Token type (ACCESS or REFRESH) which determines expiration time
     */
    public void setTokenCookie(HttpServletResponse response, String name, String value, TokenType type) {
        Cookie cookie = buildCookie(name, value, type.getMaxAge());
        response.addCookie(cookie);
    }

    /**
     * Clears a specific cookie by setting its value to {@code null} and max age to {@code 0}.
     *
     * <p>This effectively instructs the browser to delete the cookie.</p>
     *
     * @param response HTTP response to which the expired cookie will be attached
     * @param name     Name of the cookie to clear
     */
    public void clearCookie(HttpServletResponse response, String name) {
        Cookie expiredCookie = buildCookie(name, null, 0);
        response.addCookie(expiredCookie);
    }

    /**
     * Extracts the value of a cookie from an incoming HTTP request.
     *
     * <p>If the cookie is not found, returns {@code null}.</p>
     *
     * @param request HTTP request containing cookies
     * @param name    Name of the cookie to extract
     * @return Token value or {@code null} if not found
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
     * Builds a secure base cookie with common attributes such as path, HttpOnly, and Secure flags.
     *
     * @param name   Cookie name
     * @param value  Cookie value (can be {@code null} for deletion)
     * @param maxAge Cookie expiration time in seconds
     * @return Configured cookie
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
