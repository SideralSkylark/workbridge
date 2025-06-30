package com.workbridge.workbridge_app.common.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.workbridge.workbridge_app.security.UserPrincipal;

import java.io.IOException;
import java.util.UUID;

@Component
public class LoggingContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);

        MDC.put("ip", req.getRemoteAddr());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Object principal = auth.getPrincipal();

            if (principal instanceof UserPrincipal userPrincipal) {
                MDC.put("userId", String.valueOf(userPrincipal.getId()));
                MDC.put("userAgent", req.getHeader("User-Agent"));
            } else {
                MDC.put("userId", auth.getName());
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
