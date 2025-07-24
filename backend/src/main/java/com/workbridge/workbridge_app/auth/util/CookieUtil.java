package com.workbridge.workbridge_app.auth.util;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtil {

    private static final int ACCESS_TOKEN_DURATION = (int) Duration.ofMinutes(15).getSeconds();
    private static final int REFRESH_TOKEN_DURATION = (int) Duration.ofDays(7).getSeconds();
    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";


    @Value("${auth.cookie.secure}")
    private boolean isSecureCookie;

    public enum TokenType {
        ACCESS,
        REFRESH
    }

    /**
     * Sets a token as an HttpOnly, Secure cookie on the response.
     *
     * @param response the HTTP response to attach the cookie to
     * @param name     the name of the cookie
     * @param value    the JWT token value
     * @param type     token type (ACCESS or REFRESH) to determine expiry
     */
    public void setTokenCookie(HttpServletResponse response, String name, String value, TokenType type) {
        int maxAge = (type == TokenType.ACCESS) ? ACCESS_TOKEN_DURATION : REFRESH_TOKEN_DURATION;
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(isSecureCookie);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    /**
     * Clears a cookie from the response (e.g., on logout).
     *
     * @param response the HTTP response
     * @param name     the name of the cookie to clear
     */
    public void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0); // immediately expire
        cookie.setHttpOnly(true);
        cookie.setSecure(isSecureCookie);
        response.addCookie(cookie);
    }

    public String extractTokenFromCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
