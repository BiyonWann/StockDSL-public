package com.stockdsl.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT Authentication Filter
 *
 * This filter intercepts EVERY incoming HTTP request and:
 * 1. Extracts the JWT token from the Authorization header
 * 2. Validates the token
 * 3. Sets the user authentication in Spring Security context
 *
 * How it works:
 * - Client sends: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 * - Filter extracts token: eyJhbGciOiJIUzI1NiJ9...
 * - Validates with JwtUtil
 * - If valid, allows request to proceed
 * - If invalid, returns 401 Unauthorized
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Get the Authorization header
        String authHeader = request.getHeader("Authorization");

        String token = null;
        String username = null;

        // Check if Authorization header exists and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Extract token (remove "Bearer " prefix)
            token = authHeader.substring(7);

            try {
                // Extract username from token
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                // Invalid token format or expired
                logger.error("Error extracting username from token: " + e.getMessage());
            }
        }

        // If we have a username and user is not already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Validate the token
            if (jwtUtil.validateToken(token, username)) {

                // Create authentication object
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                new ArrayList<>()  // Authorities/roles (empty for now)
                        );

                // Set additional details
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in Spring Security context
                // This makes the user authenticated for this request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
