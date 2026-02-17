package com.stockdsl.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security Configuration - Sets Up Authentication and Authorization
 *
 * ============================================================
 * WHAT DOES THIS FILE DO?
 * ============================================================
 * This is the central security configuration for our Spring Boot app.
 * It defines:
 * - Which endpoints are public (no login required)
 * - Which endpoints need authentication (login required)
 * - How passwords are encoded (BCrypt)
 * - CORS settings (allow frontend to talk to backend)
 * - Session management (stateless JWT)
 *
 * ============================================================
 * ANNOTATIONS EXPLAINED
 * ============================================================
 *
 * @Configuration
 * - Tells Spring this class contains configuration settings
 * - Spring will process it at startup
 *
 * @EnableWebSecurity
 * - Enables Spring Security for this application
 * - Activates web security features
 *
 * ============================================================
 * AUTHENTICATION vs AUTHORIZATION
 * ============================================================
 *
 * Authentication: "Who are you?"
 * - Verifying identity (login with username/password)
 * - Checking JWT tokens
 *
 * Authorization: "What can you do?"
 * - Which endpoints you can access
 * - What actions you're allowed to perform
 *
 * This class configures BOTH.
 *
 * ============================================================
 * SECURITY FLOW
 * ============================================================
 *
 * 1. Request comes in (e.g., GET /api/strategies)
 * 2. Security filter chain processes it
 * 3. If endpoint is public → allow through
 * 4. If endpoint requires auth → check JWT token
 * 5. If valid token → allow through
 * 6. If invalid/missing token → return 401 Unauthorized
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ==========================================
    // DEPENDENCIES
    // ==========================================

    /**
     * JwtAuthenticationFilter - Our custom filter that validates JWT tokens.
     *
     * This filter runs before each request and:
     * 1. Extracts the JWT token from the Authorization header
     * 2. Validates the token
     * 3. Sets the user in the security context
     */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // ==========================================
    // SECURITY FILTER CHAIN
    // ==========================================

    /**
     * Configure the security filter chain
     *
     * This is the main method that configures all security settings.
     * It defines which endpoints require authentication and which don't.
     *
     * @Bean means Spring will manage this object and make it available
     * to other parts of the application.
     *
     * PUBLIC ENDPOINTS (no authentication required):
     * - POST /api/auth/login   → Anyone can log in
     * - POST /api/auth/signup  → Anyone can sign up
     *
     * PROTECTED ENDPOINTS (authentication required):
     * - GET/POST/PUT/DELETE /api/strategies/**  → Only logged-in users
     * - GET/POST /api/backtests/**              → Only logged-in users
     *
     * @param http - HttpSecurity object to configure
     * @return The configured security filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ========================================
                // CSRF PROTECTION (Disabled)
                // ========================================
                // CSRF (Cross-Site Request Forgery) protection is disabled.
                // Why? Because we use JWT tokens for authentication.
                // CSRF protection is mainly needed for cookie-based auth.
                // With JWT in headers, CSRF attacks aren't a concern.
                .csrf(csrf -> csrf.disable())

                // ========================================
                // CORS CONFIGURATION
                // ========================================
                // CORS (Cross-Origin Resource Sharing) allows our React
                // frontend (http://localhost:3000) to make requests to
                // our Spring Boot backend (http://localhost:8080).
                // Without this, the browser would block the requests.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ========================================
                // URL AUTHORIZATION RULES
                // ========================================
                // This is where we define which URLs require authentication.
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC: Allow anyone to access auth endpoints
                        // This lets users log in and sign up
                        .requestMatchers("/api/auth/**").permitAll()

                        // PROTECTED: Require authentication for all other API endpoints
                        // This includes /api/strategies, /api/backtests, etc.
                        .requestMatchers("/api/**").authenticated()

                        // ALLOW: Any other requests (static files, etc.)
                        .anyRequest().permitAll()
                )

                // ========================================
                // SESSION MANAGEMENT (Stateless)
                // ========================================
                // We don't use server-side sessions. Instead, we use JWT tokens.
                // STATELESS means the server doesn't store any session data.
                // Each request must include a valid JWT token.
                // Benefits:
                // - Scalable (no session data to share between servers)
                // - RESTful (each request is self-contained)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ========================================
                // ADD JWT FILTER
                // ========================================
                // Add our custom JWT filter BEFORE Spring's default auth filter.
                // This ensures JWT tokens are validated before other auth checks.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Build and return the security filter chain
        return http.build();
    }

    // ==========================================
    // CORS CONFIGURATION
    // ==========================================

    /**
     * Configure CORS (Cross-Origin Resource Sharing)
     *
     * CORS is a security feature in browsers that blocks requests from
     * one origin (domain:port) to another unless explicitly allowed.
     *
     * Problem without CORS:
     * - React runs on http://localhost:3000
     * - Spring Boot runs on http://localhost:8080
     * - Browser blocks requests from 3000 → 8080 (different ports!)
     *
     * Solution:
     * - Configure Spring to send CORS headers
     * - Browser sees "localhost:3000 is allowed" and permits the request
     *
     * @return CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Which origins (domains) can make requests?
        // In development: React dev server at localhost:3000
        // In production: You'd add your actual domain here
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));

        // Which HTTP methods are allowed?
        // We allow all methods our API uses
        configuration.setAllowedMethods(Arrays.asList(
                "GET",      // Read data
                "POST",     // Create data
                "PUT",      // Update data
                "DELETE",   // Delete data
                "OPTIONS"   // Preflight requests (browser sends these automatically)
        ));

        // Which headers can be sent?
        // "*" means allow all headers (including Authorization for JWT)
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Allow credentials (cookies, authorization headers)?
        // Yes, because we send the JWT token in the Authorization header
        configuration.setAllowCredentials(true);

        // Apply this configuration to all URLs
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // ==========================================
    // PASSWORD ENCODER
    // ==========================================

    /**
     * Create a PasswordEncoder bean using BCrypt
     *
     * BCrypt is a secure password hashing algorithm. It:
     * - Converts passwords to hashes (one-way, can't be reversed)
     * - Adds a random "salt" (same password → different hashes)
     * - Is intentionally slow (prevents brute force attacks)
     *
     * Example:
     * - Password: "password123"
     * - BCrypt hash: "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
     *
     * Why BCrypt?
     * - Industry standard for password hashing
     * - Built-in salt generation
     * - Adjustable work factor (can be made slower as computers get faster)
     *
     * @Bean makes this available for @Autowired injection elsewhere
     * (e.g., in AuthService for hashing and verifying passwords)
     *
     * @return A BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ==========================================
    // AUTHENTICATION MANAGER
    // ==========================================

    /**
     * Create an AuthenticationManager bean
     *
     * The AuthenticationManager is used to authenticate users.
     * It's a core Spring Security component that:
     * - Takes credentials (username/password)
     * - Validates them
     * - Returns an authenticated user or throws an exception
     *
     * We need to expose it as a bean so other classes (like AuthService)
     * can use it.
     *
     * @param config - Spring's AuthenticationConfiguration
     * @return The AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
