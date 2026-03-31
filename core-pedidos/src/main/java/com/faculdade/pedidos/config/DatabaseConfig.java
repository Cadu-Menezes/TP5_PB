package com.faculdade.pedidos.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*
    Configuração do banco de dados PostgreSQL para o módulo de pedidos.
*/
public final class DatabaseConfig {

    private static final String DEFAULT_DB_HOST = "localhost";
    private static final String DEFAULT_DB_PORT = "5432";
    private static final String DEFAULT_DB_NAME = "tp4_integrado_db";
    private static final String DEFAULT_DB_USER = "postgres";
    private static final String DEFAULT_DB_PASSWORD = "rj21DWITH052@22";

    private DatabaseConfig() {
        // Utility class
    }

    public static DataSource createDefaultDataSource() {
        ensureDatabaseExists();

        String host = getProperty("DB_HOST", DEFAULT_DB_HOST);
        String port = getProperty("DB_PORT", DEFAULT_DB_PORT);
        String dbName = getProperty("DB_NAME", DEFAULT_DB_NAME);
        String user = getProperty("DB_USER", DEFAULT_DB_USER);
        String password = getProperty("DB_PASSWORD", DEFAULT_DB_PASSWORD);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName));
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        config.setMinimumIdle(2);
        config.setMaximumPoolSize(10);
        config.setAutoCommit(true);
        config.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(config);
    }

    public static DataSource createTestDataSource() {
        HikariConfig config = new HikariConfig();
        String host = getProperty("DB_HOST", DEFAULT_DB_HOST);
        String port = getProperty("DB_PORT", DEFAULT_DB_PORT);
        String dbName = getProperty("DB_NAME", DEFAULT_DB_NAME);
        String user = getProperty("DB_USER", DEFAULT_DB_USER);
        String password = getProperty("DB_PASSWORD", DEFAULT_DB_PASSWORD);

        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName));
        config.setDriverClassName("org.postgresql.Driver");
        config.setUsername(user);
        config.setPassword(password);
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(5);
        config.setAutoCommit(true);
        config.setConnectionTestQuery("SELECT 1");
        return new HikariDataSource(config);
    }

    private static void ensureDatabaseExists() {
        String host = getProperty("DB_HOST", DEFAULT_DB_HOST);
        String port = getProperty("DB_PORT", DEFAULT_DB_PORT);
        String dbName = getProperty("DB_NAME", DEFAULT_DB_NAME);
        String user = getProperty("DB_USER", DEFAULT_DB_USER);
        String password = getProperty("DB_PASSWORD", DEFAULT_DB_PASSWORD);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%s/postgres", host, port));
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);

        try (HikariDataSource ds = new HikariDataSource(config);
             Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'")) {
            if (!rs.next()) {
                stmt.execute("CREATE DATABASE " + dbName);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao preparar banco de pedidos: " + e.getMessage(), e);
        }
    }

    private static String getProperty(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }

        value = System.getenv(key);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }

        return defaultValue;
    }
}
