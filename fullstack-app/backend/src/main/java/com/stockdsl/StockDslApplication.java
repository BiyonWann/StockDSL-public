package com.stockdsl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the StockDSL Backend Application
 *
 * This Spring Boot application provides REST APIs for:
 * - User authentication (signup/login)
 * - Strategy management (CRUD operations)
 * - Backtest execution (compile DSL and run Python)
 *
 * Architecture:
 * - Controllers: Handle HTTP requests/responses
 * - Services: Business logic
 * - Repositories: Database access (JPA)
 * - Models: Database entities
 * - Compiler: ANTLR integration for DSL → Python translation
 *
 * @author Biyon Wanninayake
 */
@SpringBootApplication
public class StockDslApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockDslApplication.class, args);
        System.out.println("\n" +
                "========================================\n" +
                "   StockDSL Backend Started!          \n" +
                "   API: http://localhost:8080         \n" +
                "========================================\n");
    }
}
