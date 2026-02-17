package com.stockdsl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * User Entity - Represents a User Account in the Database
 *
 * ============================================================
 * WHAT IS AN ENTITY?
 * ============================================================
 * An Entity is a Java class that maps to a database table.
 * Each instance of User represents one row in the 'users' table.
 *
 * Java Object          ←→    Database Row
 * ─────────────              ─────────────
 * User user = ...            INSERT INTO users ...
 * user.getUsername()         SELECT username FROM users
 *
 * ============================================================
 * JPA (Java Persistence API)
 * ============================================================
 * JPA is a specification for ORM (Object-Relational Mapping).
 * It lets us work with databases using Java objects instead of SQL.
 *
 * Without JPA:
 *   String sql = "INSERT INTO users (username, email) VALUES (?, ?)";
 *   statement.setString(1, "john");
 *   statement.setString(2, "john@email.com");
 *
 * With JPA:
 *   User user = new User();
 *   user.setUsername("john");
 *   user.setEmail("john@email.com");
 *   userRepository.save(user);  // JPA generates the SQL for us!
 *
 * ============================================================
 * DATABASE TABLE STRUCTURE
 * ============================================================
 * This class maps to the 'users' table:
 *
 * CREATE TABLE users (
 *   id            BIGSERIAL PRIMARY KEY,
 *   username      VARCHAR(50) UNIQUE NOT NULL,
 *   email         VARCHAR(255) UNIQUE NOT NULL,
 *   password_hash VARCHAR(255) NOT NULL,
 *   first_name    VARCHAR(100),
 *   last_name     VARCHAR(100),
 *   created_at    TIMESTAMP,
 *   last_login    TIMESTAMP
 * );
 *
 * ============================================================
 * RELATIONSHIPS
 * ============================================================
 * - One User has many Strategies (one-to-many)
 * - One User has many Backtests (one-to-many)
 *
 * ============================================================
 * LOMBOK ANNOTATIONS
 * ============================================================
 * @Data - Generates getters, setters, toString(), equals(), hashCode()
 * @NoArgsConstructor - Generates default constructor: User()
 * @AllArgsConstructor - Generates constructor with all fields
 *
 * Note: We also have manual getters/setters below as backup
 * in case Lombok isn't properly configured.
 */
@Entity                    // Marks this class as a JPA entity (database table)
@Table(name = "users")     // Maps to the 'users' table in PostgreSQL
@Data                      // Lombok: auto-generates getters/setters
@NoArgsConstructor         // Lombok: generates User() constructor
@AllArgsConstructor        // Lombok: generates User(all fields) constructor
public class User {

    // ==========================================
    // PRIMARY KEY
    // ==========================================

    /**
     * The unique identifier for this user.
     *
     * @Id - Marks this field as the primary key
     * @GeneratedValue - Database auto-generates the ID
     * GenerationType.IDENTITY - Uses PostgreSQL's SERIAL/BIGSERIAL
     *
     * Example: When we save a new user, PostgreSQL assigns
     * the next available ID (1, 2, 3, ...)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // USER CREDENTIALS
    // ==========================================

    /**
     * The username for login.
     *
     * @Column annotations:
     * - unique = true: No two users can have the same username
     * - nullable = false: This field is required (NOT NULL in SQL)
     * - length = 50: Maximum 50 characters (VARCHAR(50) in SQL)
     */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /**
     * The user's email address.
     *
     * - unique = true: No two users can have the same email
     * - nullable = false: Required field
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * The hashed password (NEVER store plain text passwords!)
     *
     * @JsonIgnore - This annotation is CRITICAL for security!
     * It prevents the password hash from being sent in JSON responses.
     *
     * Without @JsonIgnore, API responses would include:
     * { "username": "john", "passwordHash": "$2a$10$..." }  ← BAD!
     *
     * With @JsonIgnore, API responses are:
     * { "username": "john" }  ← GOOD! Password hash is hidden
     *
     * name = "password_hash" maps this field to the password_hash column
     */
    @Column(nullable = false, name = "password_hash")
    @JsonIgnore  // SECURITY: Never expose password hash to clients!
    private String passwordHash;

    // ==========================================
    // PROFILE INFORMATION
    // ==========================================

    /**
     * User's first name (optional).
     *
     * name = "first_name" maps to snake_case column name
     * (Java uses camelCase, SQL typically uses snake_case)
     */
    @Column(name = "first_name", length = 100)
    private String firstName;

    /**
     * User's last name (optional).
     */
    @Column(name = "last_name", length = 100)
    private String lastName;

    // ==========================================
    // TIMESTAMPS
    // ==========================================

    /**
     * When this account was created.
     *
     * Automatically set when the user is first saved (see @PrePersist below).
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Last time the user logged in.
     *
     * Updated by AuthService when user logs in.
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // ==========================================
    // RELATIONSHIPS
    // ==========================================

    /**
     * List of strategies owned by this user.
     *
     * This is a ONE-TO-MANY relationship:
     * - ONE user can have MANY strategies
     * - Each strategy belongs to exactly ONE user
     *
     * JPA Annotations explained:
     *
     * @OneToMany - Defines the relationship type
     *
     * mappedBy = "user" - The "user" field in Strategy class owns this relationship
     *   (Strategy has @ManyToOne private User user;)
     *
     * cascade = CascadeType.ALL - Operations cascade to children
     *   - Delete user → automatically delete all their strategies
     *   - Save user → automatically save their strategies
     *
     * orphanRemoval = true - If a strategy is removed from this list,
     *   delete it from the database too
     *
     * @JsonIgnore - Prevents infinite loops in JSON serialization
     *   Without this: User → Strategy → User → Strategy → ... (infinite!)
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore  // Prevents circular reference when converting to JSON
    private List<Strategy> strategies;

    // ==========================================
    // LIFECYCLE CALLBACKS
    // ==========================================

    /**
     * Automatically set createdAt timestamp before saving.
     *
     * @PrePersist - This method runs BEFORE the entity is first saved.
     *
     * Example:
     *   User user = new User();
     *   user.setUsername("john");
     *   userRepository.save(user);  // onCreate() runs here!
     *   // user.getCreatedAt() now has the current timestamp
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ==========================================
    // MANUAL GETTERS AND SETTERS
    // ==========================================
    // These are provided as backup in case Lombok isn't working.
    // Lombok's @Data annotation should generate these automatically.

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public List<Strategy> getStrategies() { return strategies; }
    public void setStrategies(List<Strategy> strategies) { this.strategies = strategies; }
}
