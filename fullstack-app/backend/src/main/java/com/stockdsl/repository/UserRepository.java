package com.stockdsl.repository;

import com.stockdsl.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User database operations
 *
 * Spring Data JPA automatically implements these methods:
 * - findByUsername() - Find user by username
 * - findByEmail() - Find user by email
 * - save() - Create or update user
 * - findById() - Find user by ID
 * - delete() - Delete user
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their username
     * Returns Optional.empty() if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by their email address
     * Returns Optional.empty() if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a username already exists
     * Useful for registration validation
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email already exists
     * Useful for registration validation
     */
    boolean existsByEmail(String email);
}
