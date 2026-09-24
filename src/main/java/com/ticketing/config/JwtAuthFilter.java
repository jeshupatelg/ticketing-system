package com.ticketing.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.model.User;
import com.ticketing.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Value("${app.jwt.header-name:Authorization}")
    private String headerName;

    @Value("${app.jwt.token-prefix:Bearer }")
    private String tokenPrefix;

    @Value("${app.jwt.default-user:admin}")
    private String defaultUsername;

    public JwtAuthFilter(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        User user = null;
        try {
            String authHeader = request.getHeader(headerName);
            String customUserHeader = request.getHeader("X-User-Name");

            if (authHeader != null && authHeader.startsWith(tokenPrefix)) {
                String token = authHeader.substring(tokenPrefix.length()).trim();
                user = parseUserFromJwt(token);
            } else if (customUserHeader != null && !customUserHeader.isBlank()) {
                String displayName = request.getHeader("X-User-Display");
                String email = request.getHeader("X-User-Email");
                user = userService.getOrCreateUser(customUserHeader, displayName != null ? displayName : customUserHeader, email);
            }

            if (user == null) {
                // Fallback to default user for local testing or unauthenticated requests
                user = userService.getOrCreateUser(defaultUsername, "System Admin", "admin@ticketing.local");
            }

            UserContextHolder.set(user);
            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }

    private User parseUserFromJwt(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                JsonNode root = objectMapper.readTree(payload);

                String username = null;
                if (root.has("preferred_username") && !root.get("preferred_username").asText().isBlank()) {
                    username = root.get("preferred_username").asText();
                } else if (root.has("username") && !root.get("username").asText().isBlank()) {
                    username = root.get("username").asText();
                } else if (root.has("sub") && !root.get("sub").asText().isBlank()) {
                    username = root.get("sub").asText();
                }

                String name = null;
                if (root.has("name") && !root.get("name").asText().isBlank()) {
                    name = root.get("name").asText();
                } else if (root.has("given_name")) {
                    String given = root.get("given_name").asText("");
                    String family = root.has("family_name") ? root.get("family_name").asText("") : "";
                    name = (given + " " + family).trim();
                }

                if (name == null || name.isBlank()) {
                    name = username;
                }

                String email = root.has("email") ? root.get("email").asText(null) : null;

                if (username != null && !username.isBlank()) {
                    return userService.getOrCreateUser(username, name, email);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract user from JWT token: {}", e.getMessage());
        }
        return null;
    }
}
