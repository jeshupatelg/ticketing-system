package com.ticketing.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Ensures that both root URLs (e.g. http://localhost:8080/)
 * and prefixed URLs (e.g. http://localhost:8080/ticketing/)
 * work seamlessly with zero 404 errors.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HostPrefixForwardingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // If servlet container already has a non-empty context-path, Spring MVC handles routing natively
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && !"/".equals(contextPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String uri = request.getRequestURI();

        // Forward /ticketing/api/* -> /api/*
        if (uri.startsWith("/ticketing/api/")) {
            String target = uri.substring("/ticketing".length());
            request.getRequestDispatcher(target).forward(request, response);
            return;
        }

        // Forward /ticketing/assets/* -> /assets/*
        if (uri.startsWith("/ticketing/assets/")) {
            String target = uri.substring("/ticketing".length());
            request.getRequestDispatcher(target).forward(request, response);
            return;
        }

        // Forward /ticketing or /ticketing/ -> /index.html
        if (uri.equals("/ticketing") || uri.equals("/ticketing/")) {
            request.getRequestDispatcher("/index.html").forward(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
