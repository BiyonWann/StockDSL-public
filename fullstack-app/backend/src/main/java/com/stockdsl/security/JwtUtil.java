package com.stockdsl.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Utility Class - Token Generation and Validation
 *
 * ============================================================
 * WHAT IS JWT (JSON Web Token)?
 * ============================================================
 * JWT is a standard for securely transmitting information between
 * parties as a JSON object. It's commonly used for authentication.
 *
 * Think of a JWT like a hotel key card:
 * - You check in (login) and get a key card (token)
 * - The key card proves you're a guest (authenticated)
 * - The key card expires after checkout time (expiration)
 * - Only the hotel can create valid key cards (signature)
 *
 * ============================================================
 * JWT TOKEN STRUCTURE
 * ============================================================
 * A JWT has three parts separated by dots: xxxxx.yyyyy.zzzzz
 *
 * 1. HEADER (xxxxx)
 *    - Algorithm used (e.g., HS256)
 *    - Type of token (JWT)
 *    Example: { "alg": "HS256", "typ": "JWT" }
 *
 * 2. PAYLOAD (yyyyy)
 *    - Claims: statements about the user
 *    - "sub": Subject (username)
 *    - "iat": Issued at (when created)
 *    - "exp": Expiration time
 *    Example: { "sub": "john", "iat": 1234567890, "exp": 1234654290 }
 *
 * 3. SIGNATURE (zzzzz)
 *    - Verifies the token wasn't tampered with
 *    - Created using: HMACSHA256(header + payload, secret)
 *
 * ============================================================
 * AUTHENTICATION FLOW WITH JWT
 * ============================================================
 *
 * 1. User logs in with username/password
 * 2. Server validates credentials against database
 * 3. Server generates JWT token with user info
 * 4. Server sends token back to client
 * 5. Client stores token (usually in localStorage)
 * 6. Client includes token in every API request:
 *    Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 * 7. Server validates token and identifies user
 *
 * ============================================================
 * WHY USE JWT?
 * ============================================================
 * - Stateless: Server doesn't need to store session data
 * - Scalable: Works across multiple servers
 * - Portable: Can be used in mobile apps, SPAs, etc.
 * - Self-contained: Token contains all user info needed
 *
 * @Component tells Spring to create a single instance of this class
 * that can be injected into other classes with @Autowired
 */
@Component
public class JwtUtil {

    // ==========================================
    // CONFIGURATION
    // ==========================================

    /**
     * SECRET_KEY - The key used to sign and verify tokens
     *
     * This is like a password that only the server knows.
     * It's used to:
     * - Sign tokens (prove they came from us)
     * - Verify tokens (check they weren't modified)
     *
     * Keys.secretKeyFor() generates a cryptographically secure key.
     *
     * IMPORTANT: In production, this should come from an environment
     * variable, not be generated at runtime. A new key means all
     * existing tokens become invalid!
     *
     * Example production setup:
     *   @Value("${jwt.secret}")
     *   private String secretKey;
     */
    private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * JWT_TOKEN_VALIDITY - How long tokens are valid (24 hours)
     *
     * After this time, the token expires and the user must log in again.
     *
     * Calculation: 24 hours * 60 minutes * 60 seconds * 1000 milliseconds
     * = 86,400,000 milliseconds = 24 hours
     *
     * Shorter validity = more secure but less convenient
     * Longer validity = more convenient but less secure
     */
    private static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60 * 1000; // 24 hours

    // ==========================================
    // TOKEN GENERATION
    // ==========================================

    /**
     * Generate a new JWT token for a user
     *
     * This is called after successful login to create a token
     * that the user will use for future requests.
     *
     * @param username - The username to put in the token
     * @return A JWT token string like "eyJhbGciOiJIUzI1NiJ9..."
     *
     * Example:
     *   String token = jwtUtil.generateToken("john");
     *   // Returns: "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIi..."
     */
    public String generateToken(String username) {
        // Create an empty map for additional claims (we're not adding any)
        Map<String, Object> claims = new HashMap<>();

        // Call the private method to actually build the token
        return createToken(claims, username);
    }

    /**
     * Create the actual JWT token
     *
     * This method builds the token using the JJWT library.
     *
     * @param claims - Additional data to include (like roles)
     * @param subject - The username (called "subject" in JWT terminology)
     * @return The complete JWT token string
     *
     * The token will contain:
     * {
     *   "sub": "john",              // The username
     *   "iat": 1705312800,          // Issued at (timestamp)
     *   "exp": 1705399200           // Expires at (timestamp)
     * }
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)                                          // Add custom claims
                .setSubject(subject)                                        // Set username
                .setIssuedAt(new Date(System.currentTimeMillis()))          // Set creation time
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))  // Set expiry
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)             // Sign with secret key
                .compact();                                                  // Build the token string
    }

    // ==========================================
    // TOKEN EXTRACTION (Reading data from token)
    // ==========================================

    /**
     * Extract the username from a token
     *
     * This is the most commonly used extraction method.
     * When a request comes in, we extract the username to know
     * who is making the request.
     *
     * @param token - The JWT token string
     * @return The username stored in the token
     *
     * Example:
     *   String username = jwtUtil.extractUsername(token);
     *   // Returns: "john"
     */
    public String extractUsername(String token) {
        // The username is stored in the "subject" claim
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract the expiration date from a token
     *
     * Used to check when a token will expire.
     *
     * @param token - The JWT token string
     * @return The expiration date
     *
     * Example:
     *   Date expiry = jwtUtil.extractExpiration(token);
     *   // Returns: Mon Jan 15 2024 10:00:00
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract any claim from the token
     *
     * This is a generic method that can extract any piece of data
     * from the token's payload.
     *
     * @param token - The JWT token string
     * @param claimsResolver - A function that extracts the specific claim
     * @return The extracted claim value
     *
     * What is Function<Claims, T>?
     * - It's a functional interface (a function you can pass around)
     * - Takes Claims as input, returns type T
     * - Claims::getSubject is shorthand for: claims -> claims.getSubject()
     *
     * Example:
     *   String username = extractClaim(token, Claims::getSubject);
     *   Date expiry = extractClaim(token, Claims::getExpiration);
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        // First, get all claims from the token
        final Claims claims = extractAllClaims(token);

        // Then, apply the resolver function to get the specific claim
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from a token
     *
     * This method parses the token and returns all the data in its payload.
     * It also verifies the signature to ensure the token wasn't tampered with.
     *
     * @param token - The JWT token string
     * @return Claims object containing all token data
     * @throws Exception if token is invalid or signature doesn't match
     *
     * What happens:
     * 1. Parse the token using our secret key
     * 2. Verify the signature matches
     * 3. Return the payload (claims)
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)   // Use our secret key to verify
                .build()
                .parseClaimsJws(token)       // Parse and verify the token
                .getBody();                   // Get the payload (claims)
    }

    // ==========================================
    // TOKEN VALIDATION
    // ==========================================

    /**
     * Check if a token has expired
     *
     * Compares the token's expiration date to the current time.
     *
     * @param token - The JWT token string
     * @return true if expired, false if still valid
     */
    private Boolean isTokenExpired(String token) {
        // Get expiration date and check if it's before now
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate a JWT token
     *
     * This is the main validation method called on every request.
     * It checks two things:
     * 1. The username in the token matches the expected user
     * 2. The token hasn't expired
     *
     * @param token - The JWT token string
     * @param username - The expected username
     * @return true if valid, false otherwise
     *
     * Example:
     *   boolean isValid = jwtUtil.validateToken(token, "john");
     *   if (isValid) {
     *       // Allow the request
     *   } else {
     *       // Reject the request (401 Unauthorized)
     *   }
     */
    public Boolean validateToken(String token, String username) {
        // Extract username from token
        final String extractedUsername = extractUsername(token);

        // Token is valid if:
        // 1. Username matches
        // 2. Token is not expired
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}
