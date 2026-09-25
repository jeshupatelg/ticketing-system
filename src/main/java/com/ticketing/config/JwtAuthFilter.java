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

    /**
     * NON-CONFIGURABLE LOCAL DEV FALLBACK FLAG:
     * In server deployment, Keycloak JWT is mandatory for all access.
     * This flag is hardcoded to false (disabled) by default and CANNOT be enabled via
     * application properties or environment variables.
     * 
     * To test locally without Keycloak/APIGW:
     * A developer must manually flip this flag in source code to true, rebuild, and redeploy.
     */
    private static final boolean ENABLE_LOCAL_DEV_FALLBACK = false;

    @Value("${app.jwt.header-name:Authorization}")
    private String headerName;

    @Value("${app.jwt.token-prefix:Bearer }")
    private String tokenPrefix;

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
            } else if (customUserHeader != null && !customUserHeader.isBlank() && ENABLE_LOCAL_DEV_FALLBACK) {
                // Custom header override only permitted if local dev fallback is manually enabled in code
                String displayName = request.getHeader("X-User-Display");
                String email = request.getHeader("X-User-Email");
                user = userService.getOrCreateUser(customUserHeader, displayName != null ? displayName : customUserHeader, email);
            }

            if (user == null) {
                if (ENABLE_LOCAL_DEV_FALLBACK) {
                    // Manual in-class dev fallback for offline testing without APIGW/Keycloak
                    user = userService.getOrCreateUser("admin", "System Admin", "admin@ticketing.local");
                } else if (isProtectedApiRequest(request)) {
                    // In server deployment, reject unauthenticated API requests immediately with 401
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Unauthorized: Mandatory Keycloak JWT token missing or invalid\"}");
                    return;
                }
            }

            UserContextHolder.set(user);
            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }

    private boolean isProtectedApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Allow static assets, public photos (default & custom), and health checks
        if (uri.endsWith("/health") || uri.contains("/photos/") || uri.endsWith(".svg") || uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".html") || uri.equals("/") || uri.endsWith("/ticketing") || uri.endsWith("/ticketing/")) {
            return false;
        }
        // Gated API calls
        return uri.contains("/api/");
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
