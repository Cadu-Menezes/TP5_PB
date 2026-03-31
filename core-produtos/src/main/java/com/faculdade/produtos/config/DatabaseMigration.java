package com.faculdade.produtos.config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/*
    Gerencia a criação e migração do esquema do banco de dados.
*/
public final class DatabaseMigration {
    
    private static final String CREATE_PRODUCTS_TABLE = """
        CREATE TABLE IF NOT EXISTS products (
            id UUID PRIMARY KEY,
            name VARCHAR(100) NOT NULL UNIQUE,
            description TEXT NOT NULL,
            price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
            category VARCHAR(50) NOT NULL,
            stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0),
            created_at TIMESTAMP NOT NULL,
            updated_at TIMESTAMP NOT NULL
        )
        """;
    
    private static final String CREATE_NAME_INDEX = 
        "CREATE INDEX IF NOT EXISTS idx_products_name ON products(name)";
    
    private static final String CREATE_CATEGORY_INDEX = 
        "CREATE INDEX IF NOT EXISTS idx_products_category ON products(category)";
    
    private static final String CREATE_STOCK_INDEX = 
        "CREATE INDEX IF NOT EXISTS idx_products_stock ON products(stock_quantity)";

    private DatabaseMigration() {
        // Utility class
    }

    /*
        Executa todas as migrações necessárias.
    */
    public static void migrate(DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            createTables(conn);
            createIndexes(conn);
        }
    }

    /*
        Remove todas as tabelas (para testes).
    */
    public static void dropAllTables(DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("DROP TABLE IF EXISTS products CASCADE");
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_PRODUCTS_TABLE);
        }
    }

    private static void createIndexes(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_NAME_INDEX);
            stmt.execute(CREATE_CATEGORY_INDEX);
            stmt.execute(CREATE_STOCK_INDEX);
        }
    }
}