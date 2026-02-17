package com.stockdsl.controller;

import com.stockdsl.model.Strategy;
import com.stockdsl.service.StrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Strategy Controller - REST API for CRUD Operations
 *
 * This controller handles all HTTP requests for managing trading strategies.
 * It implements the CRUD pattern (Create, Read, Update, Delete).
 *
 * ============================================================
 * WHAT IS CRUD?
 * ============================================================
 * CRUD is a standard pattern for data management:
 * - Create: Add new data (POST request)
 * - Read:   Get existing data (GET request)
 * - Update: Modify existing data (PUT request)
 * - Delete: Remove data (DELETE request)
 *
 * ============================================================
 * REST API ENDPOINTS
 * ============================================================
 * This controller provides 5 endpoints:
 *
 * | HTTP Method | URL                    | Description              |
 * |-------------|------------------------|--------------------------|
 * | GET         | /api/strategies        | List all strategies      |
 * | GET         | /api/strategies/{id}   | Get one strategy by ID   |
 * | POST        | /api/strategies        | Create a new strategy    |
 * | PUT         | /api/strategies/{id}   | Update existing strategy |
 * | DELETE      | /api/strategies/{id}   | Delete a strategy        |
 *
 * ============================================================
 * SECURITY
 * ============================================================
 * All endpoints require authentication (JWT token).
 * The Authentication parameter is automatically injected by Spring Security.
 * Users can only access their own strategies (enforced by the service layer).
 *
 * ============================================================
 * ANNOTATIONS EXPLAINED
 * ============================================================
 * @RestController = This class handles HTTP requests and returns JSON
 * @RequestMapping = Base URL path for all endpoints in this controller
 */
@RestController
@RequestMapping("/api/strategies")
public class StrategyController {

    // ==========================================
    // DEPENDENCY INJECTION
    // ==========================================

    /**
     * StrategyService handles the business logic.
     *
     * @Autowired tells Spring to automatically inject an instance.
     * This is called "Dependency Injection" - instead of creating
     * the service ourselves (new StrategyService()), Spring creates
     * it and gives it to us.
     *
     * Why use Dependency Injection?
     * - Easier to test (we can inject mock services)
     * - Loose coupling (controller doesn't depend on concrete implementation)
     * - Spring manages the lifecycle of the service
     */
    @Autowired
    private StrategyService strategyService;

    // ==========================================
    // READ - Get All Strategies
    // ==========================================

    /**
     * Get all strategies for the current user
     *
     * HTTP Request: GET /api/strategies
     *
     * How it works:
     * 1. Spring Security validates the JWT token in the request header
     * 2. If valid, Spring creates an Authentication object with user info
     * 3. We extract the username from the Authentication object
     * 4. We ask the service to find all strategies for this user
     * 5. We return the list as JSON
     *
     * @param auth - Injected by Spring Security from the JWT token
     *               Contains the username and authentication status
     * @return List of strategies as JSON
     *
     * Example Response:
     * [
     *   { "id": 1, "name": "AAPL Strategy", "dslCode": "strategy {...}" },
     *   { "id": 2, "name": "NVDA Strategy", "dslCode": "strategy {...}" }
     * ]
     *
     * What is @GetMapping?
     * - Tells Spring this method handles GET requests
     * - No path specified = uses the base path (/api/strategies)
     */
    @GetMapping
    public ResponseEntity<List<Strategy>> getAllStrategies(Authentication auth) {
        // Get username from the JWT token (stored in Authentication object)
        String username = auth.getName();

        // Ask the service to get all strategies for this user
        List<Strategy> strategies = strategyService.getUserStrategies(username);

        // Return 200 OK with the list of strategies
        return ResponseEntity.ok(strategies);
    }

    // ==========================================
    // READ - Get One Strategy by ID
    // ==========================================

    /**
     * Get a specific strategy by its ID
     *
     * HTTP Request: GET /api/strategies/{id}
     * Example:      GET /api/strategies/42
     *
     * How it works:
     * 1. Extract the ID from the URL path
     * 2. Get the username from the JWT token
     * 3. Ask the service to find the strategy
     * 4. Service verifies the strategy belongs to this user
     * 5. Return the strategy as JSON
     *
     * @param id - The strategy ID from the URL path
     * @param auth - The authenticated user info from JWT
     * @return The strategy as JSON
     *
     * What is @PathVariable?
     * - Extracts a value from the URL path
     * - {id} in the path becomes the 'id' parameter
     * - Example: /api/strategies/42 → id = 42
     *
     * Example Response:
     * { "id": 42, "name": "NVDA Strategy", "dslCode": "strategy {...}" }
     */
    @GetMapping("/{id}")
    public ResponseEntity<Strategy> getStrategy(
            @PathVariable Long id,      // Extracted from URL: /api/strategies/{id}
            Authentication auth) {      // Injected by Spring Security

        // Get username from JWT token
        String username = auth.getName();

        // Get the strategy (service checks ownership)
        Strategy strategy = strategyService.getStrategy(id, username);

        // Return 200 OK with the strategy
        return ResponseEntity.ok(strategy);
    }

    // ==========================================
    // CREATE - Add New Strategy
    // ==========================================

    /**
     * Create a new strategy
     *
     * HTTP Request: POST /api/strategies
     *
     * How it works:
     * 1. Client sends JSON body with strategy details
     * 2. Spring converts JSON to CreateStrategyRequest object
     * 3. We extract the username from JWT token
     * 4. We call the service to create the strategy
     * 5. Service saves it to the database
     * 6. We return the created strategy (with new ID)
     *
     * @param request - The strategy data from request body (JSON)
     * @param auth - The authenticated user info from JWT
     * @return The created strategy with its new database ID
     *
     * What is @RequestBody?
     * - Tells Spring to parse the JSON body of the request
     * - Converts JSON to a Java object automatically
     *
     * What is @PostMapping?
     * - Handles HTTP POST requests (used for creating data)
     *
     * Example Request Body:
     * {
     *   "name": "My New Strategy",
     *   "dslCode": "strategy { config { capital = 10000 } }",
     *   "description": "A simple moving average strategy"
     * }
     *
     * Example Response:
     * {
     *   "id": 43,  ← New ID assigned by database
     *   "name": "My New Strategy",
     *   "dslCode": "strategy {...}",
     *   "description": "...",
     *   "createdAt": "2024-01-15T10:30:00"
     * }
     */
    @PostMapping
    public ResponseEntity<Strategy> createStrategy(
            @RequestBody CreateStrategyRequest request,  // JSON body → Java object
            Authentication auth) {

        // Get username from JWT token
        String username = auth.getName();

        // Create the strategy in the database
        Strategy strategy = strategyService.createStrategy(
            username,
            request.getName(),
            request.getDslCode(),
            request.getDescription()
        );

        // Return 200 OK with the created strategy
        return ResponseEntity.ok(strategy);
    }

    // ==========================================
    // UPDATE - Modify Existing Strategy
    // ==========================================

    /**
     * Update an existing strategy
     *
     * HTTP Request: PUT /api/strategies/{id}
     * Example:      PUT /api/strategies/42
     *
     * How it works:
     * 1. Extract strategy ID from URL path
     * 2. Parse the JSON body with new strategy data
     * 3. Get username from JWT token
     * 4. Service finds the strategy, verifies ownership, and updates it
     * 5. Return the updated strategy
     *
     * @param id - The strategy ID from URL path
     * @param request - The new strategy data from request body
     * @param auth - The authenticated user info from JWT
     * @return The updated strategy
     *
     * What is @PutMapping?
     * - Handles HTTP PUT requests (used for updating data)
     *
     * Note: PUT replaces the entire resource. We update all fields.
     *
     * Example Request:
     * PUT /api/strategies/42
     * Body: {
     *   "name": "Updated Strategy Name",
     *   "dslCode": "strategy { ... new code ... }",
     *   "description": "New description"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<Strategy> updateStrategy(
            @PathVariable Long id,                       // From URL path
            @RequestBody UpdateStrategyRequest request,  // From JSON body
            Authentication auth) {

        // Get username from JWT token
        String username = auth.getName();

        // Update the strategy (service checks ownership)
        Strategy strategy = strategyService.updateStrategy(
            id,
            username,
            request.getName(),
            request.getDslCode(),
            request.getDescription()
        );

        // Return 200 OK with the updated strategy
        return ResponseEntity.ok(strategy);
    }

    // ==========================================
    // DELETE - Remove Strategy
    // ==========================================

    /**
     * Delete a strategy
     *
     * HTTP Request: DELETE /api/strategies/{id}
     * Example:      DELETE /api/strategies/42
     *
     * How it works:
     * 1. Extract strategy ID from URL path
     * 2. Get username from JWT token
     * 3. Service finds the strategy, verifies ownership, and deletes it
     * 4. Return 204 No Content (success, no body)
     *
     * @param id - The strategy ID to delete
     * @param auth - The authenticated user info from JWT
     * @return 204 No Content on success
     *
     * What is @DeleteMapping?
     * - Handles HTTP DELETE requests (used for deleting data)
     *
     * What is ResponseEntity<Void>?
     * - Void means no response body
     * - 204 No Content is the standard response for DELETE
     *
     * Note: This also deletes any backtests associated with this strategy!
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStrategy(
            @PathVariable Long id,
            Authentication auth) {

        // Get username from JWT token
        String username = auth.getName();

        // Delete the strategy (service checks ownership first)
        strategyService.deleteStrategy(id, username);

        // Return 204 No Content (success, empty response)
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // DTOs (Data Transfer Objects)
    // ==========================================

    /**
     * What are DTOs?
     *
     * DTO = Data Transfer Object
     *
     * DTOs are simple classes that hold data being transferred between
     * the client (frontend) and server (backend).
     *
     * Why use DTOs instead of the Strategy entity directly?
     *
     * 1. Security: We control exactly what data comes in
     *    - Client shouldn't be able to set the 'id' or 'createdAt'
     *    - Client shouldn't be able to set the 'user' field
     *
     * 2. Flexibility: Request format can differ from database format
     *    - Request might have different field names
     *    - We can validate and transform data before saving
     *
     * 3. Separation: API contract is separate from database schema
     *    - We can change the database without changing the API
     *
     * These DTOs are "inner classes" (defined inside the controller)
     * because they're only used by this controller.
     */

    /**
     * DTO for creating a new strategy
     *
     * Expected JSON format:
     * {
     *   "name": "Strategy Name",
     *   "dslCode": "strategy { ... }",
     *   "description": "Optional description"
     * }
     *
     * Note: No 'id' field because the database assigns the ID.
     */
    public static class CreateStrategyRequest {
        private String name;
        private String dslCode;
        private String description;

        // Getters - Spring uses these to read the data
        public String getName() { return name; }
        public String getDslCode() { return dslCode; }
        public String getDescription() { return description; }

        // Setters - Spring uses these when converting JSON to object
        public void setName(String name) { this.name = name; }
        public void setDslCode(String dslCode) { this.dslCode = dslCode; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * DTO for updating an existing strategy
     *
     * Same fields as CreateStrategyRequest.
     * The ID comes from the URL path, not the body.
     */
    public static class UpdateStrategyRequest {
        private String name;
        private String dslCode;
        private String description;

        // Getters
        public String getName() { return name; }
        public String getDslCode() { return dslCode; }
        public String getDescription() { return description; }

        // Setters
        public void setName(String name) { this.name = name; }
        public void setDslCode(String dslCode) { this.dslCode = dslCode; }
        public void setDescription(String description) { this.description = description; }
    }
}
