package com.stockdsl.service;

import com.stockdsl.model.Strategy;
import com.stockdsl.model.User;
import com.stockdsl.repository.StrategyRepository;
import com.stockdsl.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Strategy Service - Business Logic for Strategy Management
 *
 * ============================================================
 * WHAT DOES THIS SERVICE DO?
 * ============================================================
 * This service handles all CRUD operations for trading strategies:
 * - Create: Save a new strategy to the database
 * - Read: Get one or more strategies from the database
 * - Update: Modify an existing strategy
 * - Delete: Remove a strategy from the database
 *
 * ============================================================
 * SECURITY: OWNERSHIP VERIFICATION
 * ============================================================
 * A critical feature of this service is that it ensures users
 * can ONLY access their own strategies.
 *
 * Example attack scenario (without verification):
 * - User A has strategy with ID = 5
 * - Malicious User B guesses the ID and requests GET /strategies/5
 * - Without verification, User B could see User A's strategy!
 *
 * Our protection:
 * - Every method that accesses a strategy checks:
 *   "Does this strategy belong to the requesting user?"
 * - If not, we throw an "Access denied" error
 *
 * ============================================================
 * ARCHITECTURE: WHY HAVE A SERVICE LAYER?
 * ============================================================
 *
 * Controller → Service → Repository → Database
 *
 * - Controller: Handles HTTP (receives requests, sends responses)
 * - Service: Contains business logic (rules, validation, security)
 * - Repository: Handles database queries (SQL generation)
 *
 * Benefits:
 * - Separation of concerns (each layer has one job)
 * - Testability (we can test business logic without HTTP/database)
 * - Reusability (multiple controllers can use the same service)
 */
@Service
public class StrategyService {

    // ==========================================
    // DEPENDENCIES (Injected by Spring)
    // ==========================================

    /**
     * StrategyRepository - Handles database operations for strategies.
     *
     * Spring Data JPA automatically generates SQL queries for us:
     * - findById(id) → SELECT * FROM strategies WHERE id = ?
     * - findByUserId(userId) → SELECT * FROM strategies WHERE user_id = ?
     * - save(strategy) → INSERT INTO strategies ... or UPDATE ...
     * - delete(strategy) → DELETE FROM strategies WHERE id = ?
     */
    @Autowired
    private StrategyRepository strategyRepository;

    /**
     * UserRepository - Handles database operations for users.
     *
     * We need this to look up users by username, since the JWT token
     * only contains the username, not the user ID.
     */
    @Autowired
    private UserRepository userRepository;

    // ==========================================
    // READ - Get All User's Strategies
    // ==========================================

    /**
     * Get all strategies belonging to a user
     *
     * This method returns every strategy that belongs to the specified user.
     * Used to populate the dashboard with the user's strategy list.
     *
     * How it works:
     * 1. Find the user by username
     * 2. Query the database for all strategies with that user's ID
     * 3. Return the list of strategies
     *
     * @param username - The username from the JWT token
     * @return List of all strategies belonging to this user
     * @throws RuntimeException if user is not found
     *
     * Example:
     *   List<Strategy> strategies = strategyService.getUserStrategies("john");
     *   // Returns: [Strategy1, Strategy2, Strategy3, ...]
     */
    public List<Strategy> getUserStrategies(String username) {
        // Step 1: Find the user by username
        // We need the user's ID to query their strategies
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 2: Get all strategies for this user
        // This generates: SELECT * FROM strategies WHERE user_id = ?
        return strategyRepository.findByUserId(user.getId());
    }

    // ==========================================
    // READ - Get One Strategy by ID
    // ==========================================

    /**
     * Get a specific strategy by ID (with ownership verification)
     *
     * This method retrieves a single strategy and verifies that
     * it belongs to the requesting user. This is a SECURITY feature.
     *
     * How it works:
     * 1. Find the user by username
     * 2. Find the strategy by ID
     * 3. VERIFY the strategy belongs to this user
     * 4. Return the strategy (or throw error if not authorized)
     *
     * @param id - The strategy ID to retrieve
     * @param username - The username from the JWT token (for verification)
     * @return The strategy if authorized
     * @throws RuntimeException if strategy not found or access denied
     *
     * Security Example:
     *   // User "john" tries to access strategy ID 5
     *   // Strategy 5 belongs to user "jane"
     *   getStrategy(5, "john");  // Throws "Access denied"
     *
     *   // User "jane" tries to access strategy ID 5
     *   // Strategy 5 belongs to user "jane"
     *   getStrategy(5, "jane");  // Returns the strategy
     */
    public Strategy getStrategy(Long id, String username) {
        // Step 1: Find the user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 2: Find the strategy by ID
        Strategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Strategy not found"));

        // Step 3: SECURITY CHECK - Verify ownership
        // The strategy's user ID must match the requesting user's ID
        if (!strategy.getUser().getId().equals(user.getId())) {
            // This is an unauthorized access attempt!
            throw new RuntimeException("Access denied: Strategy does not belong to this user");
        }

        // Step 4: Return the strategy (user is authorized)
        return strategy;
    }

    // ==========================================
    // CREATE - Add New Strategy
    // ==========================================

    /**
     * Create a new strategy for a user
     *
     * This method creates a new strategy and links it to the user.
     *
     * How it works:
     * 1. Find the user by username
     * 2. Create a new Strategy object
     * 3. Set all the fields (name, code, description)
     * 4. Link the strategy to the user
     * 5. Save to the database
     * 6. Return the saved strategy (with new ID)
     *
     * @param username - The username from the JWT token
     * @param name - The name of the strategy
     * @param dslCode - The DSL code (the trading rules)
     * @param description - Optional description
     * @return The created strategy with its database-assigned ID
     *
     * Example:
     *   Strategy s = strategyService.createStrategy(
     *       "john",
     *       "AAPL RSI Strategy",
     *       "strategy { ... }",
     *       "Buy AAPL when RSI is low"
     *   );
     *   System.out.println(s.getId());  // 42 (new ID from database)
     */
    public Strategy createStrategy(String username, String name, String dslCode, String description) {
        // Step 1: Find the user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 2: Create a new Strategy object
        Strategy strategy = new Strategy();

        // Step 3: Link the strategy to the user
        // This sets the foreign key relationship in the database
        strategy.setUser(user);

        // Step 4: Set the strategy details
        strategy.setName(name);
        strategy.setDslCode(dslCode);
        strategy.setDescription(description);
        strategy.setIsPublic(false);  // Private by default

        // Step 5: Save to database and return
        // save() returns the saved entity with the new ID
        return strategyRepository.save(strategy);
    }

    // ==========================================
    // UPDATE - Modify Existing Strategy
    // ==========================================

    /**
     * Update an existing strategy
     *
     * This method modifies a strategy that already exists in the database.
     * It first verifies that the user owns the strategy.
     *
     * How it works:
     * 1. Get the strategy (with ownership verification)
     * 2. Update only the fields that are provided (not null)
     * 3. Save the changes to the database
     * 4. Return the updated strategy
     *
     * @param id - The strategy ID to update
     * @param username - The username from JWT (for verification)
     * @param name - New name (or null to keep existing)
     * @param dslCode - New DSL code (or null to keep existing)
     * @param description - New description (or null to keep existing)
     * @return The updated strategy
     *
     * Note: We only update fields that are not null.
     * This allows partial updates (e.g., change only the name).
     *
     * Example:
     *   // Update only the name, keep other fields the same
     *   strategyService.updateStrategy(5, "john", "New Name", null, null);
     */
    public Strategy updateStrategy(Long id, String username, String name, String dslCode, String description) {
        // Step 1: Get the strategy (this also verifies ownership!)
        // getStrategy() will throw an error if user doesn't own it
        Strategy strategy = getStrategy(id, username);

        // Step 2: Update fields (only if new values are provided)
        // This allows partial updates - user can change just one field
        if (name != null) {
            strategy.setName(name);
        }
        if (dslCode != null) {
            strategy.setDslCode(dslCode);
        }
        if (description != null) {
            strategy.setDescription(description);
        }

        // Step 3: Save the updated strategy to database
        // JPA will generate an UPDATE statement
        return strategyRepository.save(strategy);
    }

    // ==========================================
    // DELETE - Remove Strategy
    // ==========================================

    /**
     * Delete a strategy from the database
     *
     * This method permanently removes a strategy.
     * It first verifies that the user owns the strategy.
     *
     * How it works:
     * 1. Get the strategy (with ownership verification)
     * 2. Delete it from the database
     *
     * @param id - The strategy ID to delete
     * @param username - The username from JWT (for verification)
     *
     * Security: Only the owner can delete their strategy.
     * getStrategy() handles the ownership check for us.
     *
     * Important: This is a permanent action! The strategy cannot be recovered.
     * In a production app, you might want "soft delete" (mark as deleted
     * instead of actually removing from database).
     *
     * Example:
     *   strategyService.deleteStrategy(5, "john");
     *   // Strategy 5 is now permanently deleted
     */
    public void deleteStrategy(Long id, String username) {
        // Step 1: Get the strategy (this also verifies ownership!)
        // If user doesn't own the strategy, an exception is thrown
        Strategy strategy = getStrategy(id, username);

        // Step 2: Delete from database
        // JPA will generate: DELETE FROM strategies WHERE id = ?
        strategyRepository.delete(strategy);
    }
}
