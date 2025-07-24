package com.workbridge.workbridge_app.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.workbridge.workbridge_app.common.util.JsonWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwt = null;
        String username = null;
        String path = request.getServletPath();

        // 1. Ignore WebSocket paths
        if (path.startsWith("/ws-chat")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Try to get JWT from HttpOnly cookie
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        // 3. If not in cookie, try Authorization header (for Postman/dev)
        if (jwt == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                jwt = authHeader.substring(BEARER_PREFIX.length());
            }
        }

        // 4. If no token is present, skip auth and continue the chain
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception exception) {
            log.warn("Failed to extract username from JWT. Reason: {}", exception.getMessage());
            var errorResponse = com.workbridge.workbridge_app.common.response.ErrorResponse.of(
                org.springframework.http.HttpStatus.UNAUTHORIZED,
                "Unauthorized - Invalid JWT token",
                request.getRequestURI()
            );
            JsonWriter.write(response, errorResponse, HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 5. If username is present and not already authenticated, authenticate
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                var authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.info("Authenticated user '{}' via {}", username,
                    request.getCookies() != null ? "cookie" : "Authorization header");

            } else {
                log.warn("JWT token for user '{}' is invalid or expired", username);
                var errorResponse = com.workbridge.workbridge_app.common.response.ErrorResponse.of(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Unauthorized - Token invalid or expired",
                    request.getRequestURI()
                );
                JsonWriter.write(response, errorResponse, HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
