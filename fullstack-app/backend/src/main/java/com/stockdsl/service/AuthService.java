package com.stockdsl.service;

import com.stockdsl.model.User;
import com.stockdsl.repository.UserRepository;
import com.stockdsl.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Authentication Service - Handles User Registration and Login
 *
 * ============================================================
 * WHAT IS A SERVICE?
 * ============================================================
 * In Spring, a Service class contains the business logic.
 *
 * Architecture layers:
 * - Controller: Handles HTTP requests (what endpoints exist)
 * - Service: Business logic (what the app actually does)
 * - Repository: Database access (how we save/load data)
 *
 * Why separate them?
 * - Easier to test (we can test business logic without HTTP)
 * - Cleaner code (each layer has one responsibility)
 * - Reusability (services can be used by multiple controllers)
 *
 * ============================================================
 * WHAT IS @Service?
 * ============================================================
 * @Service is a Spring annotation that:
 * 1. Marks this class as a service component
 * 2. Allows Spring to create and manage instances of it
 * 3. Enables dependency injection (other classes can @Autowire it)
 *
 * ============================================================
 * AUTHENTICATION FLOW
 * ============================================================
 *
 * SIGNUP:
 * 1. User submits username, email, password
 * 2. We check if username/email already exists
 * 3. We hash the password (never store plain text!)
 * 4. We save the user to the database
 * 5. We generate a JWT token
 * 6. We return the token to the client
 *
 * LOGIN:
 * 1. User submits username and password
 * 2. We find the user in the database
 * 3. We verify the password matches the hash
 * 4. We generate a JWT token
 * 5. We return the token to the client
 */
@Service
public class AuthService {

    // ==========================================
    // DEPENDENCIES (Injected by Spring)
    // ==========================================

    /**
     * UserRepository - Handles database operations for users.
     *
     * This is a Spring Data JPA repository that provides:
     * - findByUsername(): Find a user by their username
     * - existsByUsername(): Check if a username is taken
     * - save(): Save a user to the database
     *
     * We don't write SQL - Spring Data generates it for us!
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * PasswordEncoder - Handles password hashing with BCrypt.
     *
     * BCrypt is a secure password hashing algorithm that:
     * - Converts "password123" → "$2a$10$N9qo8uLOickgx2ZMRZoMy..."
     * - Is slow on purpose (prevents brute force attacks)
     * - Includes a random "salt" (same password = different hashes)
     * - Is one-way (you can't reverse the hash to get the password)
     *
     * Methods:
     * - encode(password): Converts plain text to hash
     * - matches(plain, hash): Checks if password matches hash
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * JwtUtil - Handles JWT token generation and validation.
     *
     * JWT (JSON Web Token) is used for authentication:
     * - After login, we give the user a token
     * - User sends this token with every request
     * - We verify the token to identify the user
     *
     * The token contains:
     * - Username (who is this?)
     * - Expiration time (when does this expire?)
     * - Signature (proof this token is legit)
     */
    @Autowired
    private JwtUtil jwtUtil;

    // ==========================================
    // SIGNUP - Create New Account
    // ==========================================

    /**
     * Register a new user account
     *
     * This method handles the full signup process:
     * 1. Validate that username is not already taken
     * 2. Validate that email is not already used
     * 3. Hash the password using BCrypt
     * 4. Save the new user to the database
     * 5. Generate a JWT token for immediate login
     * 6. Return the token to the client
     *
     * @param username - The desired username (must be unique)
     * @param email - The user's email address (must be unique)
     * @param password - The plain text password (will be hashed)
     * @return AuthResponse containing the JWT token and username
     * @throws RuntimeException if username or email already exists
     *
     * Example:
     *   AuthResponse response = authService.signup("john", "john@email.com", "secret123");
     *   String token = response.getToken();  // "eyJhbGciOiJIUzI1NiJ9..."
     */
    public AuthResponse signup(String username, String email, String password) {
        // Step 1: Check if username is already taken
        // This prevents duplicate accounts
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        // Step 2: Check if email is already used
        // Each email should only be associated with one account
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        // Step 3: Create a new User object
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);

        // Step 4: Hash the password before storing
        // NEVER store plain text passwords in the database!
        // passwordEncoder.encode() converts "password123" → "$2a$10$..."
        user.setPasswordHash(passwordEncoder.encode(password));

        // Step 5: Save the user to the database
        // Spring Data JPA handles the SQL INSERT statement
        userRepository.save(user);

        // Step 6: Generate a JWT token
        // This allows the user to be immediately logged in after signup
        String token = jwtUtil.generateToken(username);

        // Return the token and username to the client
        return new AuthResponse(token, username);
    }

    // ==========================================
    // LOGIN - Authenticate Existing User
    // ==========================================

    /**
     * Authenticate a user and return a JWT token
     *
     * This method handles the login process:
     * 1. Find the user in the database by username
     * 2. Verify the password matches the stored hash
     * 3. Update the last login timestamp
     * 4. Generate a fresh JWT token
     * 5. Return the token to the client
     *
     * @param username - The username to authenticate
     * @param password - The plain text password to verify
     * @return AuthResponse containing the JWT token and username
     * @throws RuntimeException if username doesn't exist or password is wrong
     *
     * Security note:
     * We use the same error message "Invalid username or password" for both
     * wrong username and wrong password. This prevents attackers from knowing
     * whether a username exists in our system.
     *
     * Example:
     *   AuthResponse response = authService.login("john", "secret123");
     *   String token = response.getToken();
     */
    public AuthResponse login(String username, String password) {
        // Step 1: Find the user in the database
        // Optional<User> means the result might be empty (user not found)
        Optional<User> userOpt = userRepository.findByUsername(username);

        // If user doesn't exist, throw an error
        if (userOpt.isEmpty()) {
            // Generic message to not reveal if username exists
            throw new RuntimeException("Invalid username or password");
        }

        // Get the actual User object from the Optional
        User user = userOpt.get();

        // Step 2: Verify the password
        // passwordEncoder.matches() compares plain text to the hash
        // It returns true if they match, false otherwise
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            // Generic message to not reveal that username was correct
            throw new RuntimeException("Invalid username or password");
        }

        // Step 3: Update last login timestamp
        // This is useful for tracking user activity
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Step 4: Generate a fresh JWT token
        // Even if the user had an old token, we create a new one
        String token = jwtUtil.generateToken(username);

        // Return the token and username to the client
        return new AuthResponse(token, username);
    }

    // ==========================================
    // GET USER BY USERNAME
    // ==========================================

    /**
     * Find a user by their username
     *
     * This is a utility method used by other parts of the application
     * when they need to look up a user by username.
     *
     * @param username - The username to search for
     * @return The User object
     * @throws RuntimeException if user is not found
     *
     * What is orElseThrow()?
     * - Optional.orElseThrow() returns the value if present
     * - If the Optional is empty, it throws the specified exception
     * - This is a clean way to handle "not found" cases
     *
     * Example:
     *   User user = authService.getUserByUsername("john");
     *   System.out.println(user.getEmail());  // "john@email.com"
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ==========================================
    // RESPONSE DTO
    // ==========================================

    /**
     * AuthResponse - Data Transfer Object for authentication responses
     *
     * This class holds the data we send back to the client after
     * successful login or signup.
     *
     * Why use a DTO?
     * - Clean separation between internal User model and API response
     * - We control exactly what data the client receives
     * - We don't accidentally expose sensitive data (like password hash)
     *
     * The response JSON will look like:
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9...",
     *   "username": "john"
     * }
     *
     * This is an "inner class" because it's only used by AuthService.
     * The "static" keyword means it doesn't need an instance of AuthService.
     */
    public static class AuthResponse {
        private String token;      // The JWT token for authentication
        private String username;   // The authenticated user's username

        /**
         * Constructor - creates an AuthResponse with token and username
         */
        public AuthResponse(String token, String username) {
            this.token = token;
            this.username = username;
        }

        // Getters and Setters
        // These are used by Spring to convert the object to JSON

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
