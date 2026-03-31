package com.faculdade.produtos.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

/*
    Configuração do banco de dados PostgreSQL.
    Utiliza HikariCP para pool de conexões.
*/
public final class DatabaseConfig {
    
    // Configurações padrão
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "5432";
    private static final String DEFAULT_DATABASE = "produtos_db";
    private static final String DEFAULT_USERNAME = "postgres";
    private static final String DEFAULT_PASSWORD = "rj21DWITH052@22";
    
    // Configurações do pool
    private static final int DEFAULT_MINIMUM_IDLE = 2;
    private static final int DEFAULT_MAXIMUM_POOL_SIZE = 10;
    private static final long DEFAULT_MAX_LIFETIME = 300000; // 5 minutos
    private static final long DEFAULT_CONNECTION_TIMEOUT = 30000; // 30 segundos

    private DatabaseConfig() {
        // Utility class - não deve ser instanciada
    }

    /*
        Verifica se o banco de dados existe e cria caso não exista.
        Conecta ao banco padrão 'postgres' para executar a verificação.
    */
    public static void ensureDatabaseExists() {
        String host = getProperty("DB_HOST", DEFAULT_HOST);
        String port = getProperty("DB_PORT", DEFAULT_PORT);
        String database = getProperty("DB_NAME", DEFAULT_DATABASE);
        String username = getProperty("DB_USER", DEFAULT_USERNAME);
        String password = getProperty("DB_PASSWORD", DEFAULT_PASSWORD);

        // Conecta ao banco 'postgres' (sempre existe) para verificar/criar o banco alvo
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%s/postgres", host, port));
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);

        try (HikariDataSource ds = new HikariDataSource(config);
             Connection conn = ds.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                     "SELECT 1 FROM pg_database WHERE datname = '" + database + "'")) {

            if (!rs.next()) {
                System.out.println("Banco '" + database + "' não encontrado. Criando...");
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("CREATE DATABASE " + database);
                }
                System.out.println("✅ Banco '" + database + "' criado com sucesso!");
            } else {
                System.out.println("Banco '" + database + "' encontrado.");
            }
        } catch (SQLException e) {
            System.err.println("⚠ Não foi possível verificar/criar o banco: " + e.getMessage());
        }
    }

    /*
        Cria DataSource com configurações padrão.
    */
    public static DataSource createDefaultDataSource() {
        return createDataSource(
                getProperty("DB_HOST", DEFAULT_HOST),
                getProperty("DB_PORT", DEFAULT_PORT),
                getProperty("DB_NAME", DEFAULT_DATABASE),
                getProperty("DB_USER", DEFAULT_USERNAME),
                getProperty("DB_PASSWORD", DEFAULT_PASSWORD)
        );
    }

    /*
        Cria DataSource com configurações customizadas.
    */
    public static DataSource createDataSource(String host, String port, String database, 
                                            String username, String password) {
        HikariConfig config = new HikariConfig();
        
        // JDBC URL
        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, database);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        
        // Driver
        config.setDriverClassName("org.postgresql.Driver");
        
        // Pool settings
        config.setMinimumIdle(DEFAULT_MINIMUM_IDLE);
        config.setMaximumPoolSize(DEFAULT_MAXIMUM_POOL_SIZE);
        config.setMaxLifetime(DEFAULT_MAX_LIFETIME);
        config.setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);
        
        // Connection settings
        config.setAutoCommit(true);
        config.setConnectionTestQuery("SELECT 1");
        
        // Performance settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        return new HikariDataSource(config);
    }

    /*
        Cria DataSource para testes com H2.
    */
    public static DataSource createTestDataSource() {
        HikariConfig config = new HikariConfig();
        
        config.setJdbcUrl("jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        config.setDriverClassName("org.h2.Driver");
        config.setUsername("sa");
        config.setPassword("");
        
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(5);
        config.setAutoCommit(true);
        
        return new HikariDataSource(config);
    }

    /*
        Obtém propriedade do sistema ou variável de ambiente, com fallback para valor padrão.
    */
    private static String getProperty(String key, String defaultValue) {
        // Primeiro tenta propriedade do sistema
        String value = System.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }
        
        // Depois tenta variável de ambiente
        value = System.getenv(key);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }
        
        // Por fim, retorna valor padrão
        return defaultValue;
    }
}