package com.workbridge.workbridge_app.security;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;

    @Override
    public boolean beforeHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Map<String, Object> attributes
    ) throws Exception {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");

            if (token != null) {
                try {
                    String username = jwtService.extractUsername(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtService.isTokenValid(token, userDetails)) {
                        attributes.put("user", userDetails);
                        log.info("WebSocket handshake accepted for user: {}", username);
                        return true;
                    } else {
                        log.warn("WebSocket handshake denied: invalid or expired JWT for user: {}", username);
                    }
                } catch (Exception e) {
                    log.warn("WebSocket handshake failed: JWT parsing error - {}", e.getMessage());
                }
            } else {
                log.warn("WebSocket handshake denied: missing JWT token in request parameter");
            }
        }

        response.setStatusCode(HttpStatus.FORBIDDEN);
        return false;
    }

    @Override
    public void afterHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Exception exception
    ) {}
}
