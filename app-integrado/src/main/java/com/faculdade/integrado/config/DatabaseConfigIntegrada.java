package com.faculdade.integrado.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConfigIntegrada {

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "5432";
    private static final String DEFAULT_DATABASE = "tp4_integrado_db";
    private static final String DEFAULT_USERNAME = "postgres";
    private static final String DEFAULT_PASSWORD = "rj21DWITH052@22";

    private DatabaseConfigIntegrada() {
    }

    public static DataSource createDefaultDataSource() {
        ensureDatabaseExists();

        String host = getProperty("DB_HOST", DEFAULT_HOST);
        String port = getProperty("DB_PORT", DEFAULT_PORT);
        String database = getProperty("DB_NAME", DEFAULT_DATABASE);
        String username = getProperty("DB_USER", DEFAULT_USERNAME);
        String password = getProperty("DB_PASSWORD", DEFAULT_PASSWORD);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%s/%s", host, port, database));
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMinimumIdle(2);
        config.setMaximumPoolSize(10);
        config.setAutoCommit(true);
        config.setConnectionTestQuery("SELECT 1");
        return new HikariDataSource(config);
    }

    private static void ensureDatabaseExists() {
        String host = getProperty("DB_HOST", DEFAULT_HOST);
        String port = getProperty("DB_PORT", DEFAULT_PORT);
        String database = getProperty("DB_NAME", DEFAULT_DATABASE);
        String username = getProperty("DB_USER", DEFAULT_USERNAME);
        String password = getProperty("DB_PASSWORD", DEFAULT_PASSWORD);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%s/postgres", host, port));
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);

        try (HikariDataSource ds = new HikariDataSource(config);
             Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + database + "'")) {
            if (!rs.next()) {
                stmt.execute("CREATE DATABASE " + database);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao preparar banco PostgreSQL: " + e.getMessage(), e);
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
