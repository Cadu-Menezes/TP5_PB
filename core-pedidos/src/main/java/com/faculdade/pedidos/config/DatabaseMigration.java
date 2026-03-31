package com.faculdade.pedidos.config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/*
    Migração simples (sem Flyway) para garantir que as tabelas existam.
*/
public final class DatabaseMigration {

    private DatabaseMigration() {
        // Utility class
    }

    public static void migrate(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS pedidos (
                    id VARCHAR(36) PRIMARY KEY,
                    produto_id VARCHAR(36),
                    nome_cliente VARCHAR(100) NOT NULL,
                    observacao VARCHAR(255),
                    status VARCHAR(20) NOT NULL,
                    criado_em TIMESTAMP NOT NULL,
                    atualizado_em TIMESTAMP NOT NULL
                )
            """);

            stmt.executeUpdate("ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS produto_id VARCHAR(36)");

            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_pedidos_atualizado_em ON pedidos(atualizado_em)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_pedidos_produto_id ON pedidos(produto_id)");
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao migrar banco de dados: " + e.getMessage(), e);
        }
    }
}
