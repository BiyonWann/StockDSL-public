package com.stockdsl.controller;

import com.stockdsl.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST Controller
 *
 * This controller handles user registration and login.
 * It's the entry point for all authentication-related HTTP requests.
 *
 * REST API Endpoints:
 * - POST /api/auth/signup  - Register a new user account
 * - POST /api/auth/login   - Log in to an existing account
 *
 * Key Spring Annotations Explained:
 *
 * @RestController
 *   - Combines @Controller and @ResponseBody
 *   - Methods return data directly (as JSON), not HTML views
 *   - Spring automatically converts Java objects to JSON
 *
 * @RequestMapping("/api/auth")
 *   - Base URL path for all endpoints in this controller
 *   - All methods will be under /api/auth/...
 *
 * These endpoints are PUBLIC (no JWT token required) because:
 * - Users need to login/signup before they have a token
 * - SecurityConfig.java marks /api/auth/** as permitAll()
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * @Autowired tells Spring to automatically inject the AuthService
     *
     * This is called "Dependency Injection" - Spring creates the AuthService
     * instance and gives it to us. We don't create it ourselves with "new".
     *
     * Benefits of Dependency Injection:
     * - Loose coupling: Controller doesn't depend on specific implementation
     * - Testability: Easy to mock AuthService in unit tests
     * - Configuration: Spring manages the lifecycle of the service
     */
    @Autowired
    private AuthService authService;

    // ==========================================
    // SIGNUP ENDPOINT
    // ==========================================

    /**
     * User Signup/Registration Endpoint
     *
     * HTTP Method: POST
     * URL: /api/auth/signup
     *
     * How it works:
     * 1. Client sends HTTP POST with JSON body containing username, email, password
     * 2. Spring automatically converts JSON to SignupRequest object (@RequestBody)
     * 3. We call authService.signup() to create the user
     * 4. If successful, return 200 OK with token
     * 5. If error (e.g., username exists), return 400 Bad Request
     *
     * Request Body Example:
     * {
     *   "username": "john_doe",
     *   "email": "john@example.com",
     *   "password": "password123"
     * }
     *
     * Success Response (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "username": "john_doe"
     * }
     *
     * Error Response (400 Bad Request):
     * {
     *   "error": "Username already exists"
     * }
     *
     * @param request - The signup data from the client (automatically parsed from JSON)
     * @return ResponseEntity with token (success) or error message (failure)
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            // Call the service layer to handle business logic
            // The service will:
            // 1. Check if username/email already exists
            // 2. Hash the password using BCrypt
            // 3. Save user to database
            // 4. Generate and return JWT token
            AuthService.AuthResponse response = authService.signup(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword()
            );

            // Return 200 OK with the response (token + username)
            // ResponseEntity.ok() creates a 200 response
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Something went wrong (e.g., username already taken)
            // Return 400 Bad Request with error message
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // ==========================================
    // LOGIN ENDPOINT
    // ==========================================

    /**
     * User Login Endpoint
     *
     * HTTP Method: POST
     * URL: /api/auth/login
     *
     * How it works:
     * 1. Client sends HTTP POST with JSON body containing username and password
     * 2. Spring automatically converts JSON to LoginRequest object
     * 3. We call authService.login() to verify credentials
     * 4. If correct, return 200 OK with token
     * 5. If wrong, return 401 Unauthorized
     *
     * Request Body Example:
     * {
     *   "username": "john_doe",
     *   "password": "password123"
     * }
     *
     * Success Response (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "username": "john_doe"
     * }
     *
     * Error Response (401 Unauthorized):
     * {
     *   "error": "Invalid username or password"
     * }
     *
     * @param request - The login credentials from the client
     * @return ResponseEntity with token (success) or error message (failure)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Call the service layer to verify credentials
            // The service will:
            // 1. Find user by username in database
            // 2. Compare password with stored hash using BCrypt
            // 3. If match, generate and return JWT token
            // 4. If no match, throw exception
            AuthService.AuthResponse response = authService.login(
                    request.getUsername(),
                    request.getPassword()
            );

            // Return 200 OK with the response
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Invalid credentials
            // Return 401 Unauthorized (not 400) for failed authentication
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // ==========================================
    // DTO CLASSES (Data Transfer Objects)
    // ==========================================

    /**
     * DTOs are simple classes that hold data for transferring between
     * client and server. They have no business logic, just getters/setters.
     *
     * Why use DTOs instead of entity classes?
     * - Security: Don't expose internal fields (like password hash)
     * - Flexibility: Request/response format can differ from database schema
     * - Validation: Can add validation rules specific to the API
     */

    /**
     * SignupRequest DTO
     *
     * Represents the JSON data sent by client during registration.
     * Spring automatically converts JSON to this object.
     *
     * Required fields:
     * - username: The desired username
     * - email: User's email address
     * - password: Plain text password (will be hashed by backend)
     */
    public static class SignupRequest {
        private String username;
        private String email;
        private String password;

        // Getters and Setters - required for Spring to set/get values
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * LoginRequest DTO
     *
     * Represents the JSON data sent by client during login.
     */
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * ErrorResponse DTO
     *
     * Standard format for error responses.
     * Having a consistent error format makes it easier for clients to handle errors.
     */
    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
