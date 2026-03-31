package com.faculdade.produtos.repository.impl;

import com.faculdade.produtos.exception.ExcecaoBancoDados;
import com.faculdade.produtos.model.*;
import com.faculdade.produtos.repository.RepositorioComandoProduto;
import com.faculdade.produtos.repository.RepositorioConsultaProduto;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Implementação PostgreSQL do repositório de produtos.
 * Combina command e query repositories em implementação coesa.
 */
public final class RepositorioPostgreSqlProduto implements RepositorioComandoProduto, RepositorioConsultaProduto {
    
    private static final String NOME_TABELA = "products";
    
    // Queries SQL
    private static final String SQL_INSERIR = 
        "INSERT INTO " + NOME_TABELA + " (id, name, description, price, category, stock_quantity, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SQL_ATUALIZAR = 
        "UPDATE " + NOME_TABELA + " SET name = ?, description = ?, price = ?, category = ?, " +
        "stock_quantity = ?, updated_at = ? WHERE id = ?";
    
    private static final String SQL_EXCLUIR_POR_ID = 
        "DELETE FROM " + NOME_TABELA + " WHERE id = ?";
    
    private static final String SQL_EXCLUIR_TODOS = 
        "DELETE FROM " + NOME_TABELA;
    
    private static final String SQL_BUSCAR_POR_ID = 
        "SELECT id, name, description, price, category, stock_quantity, created_at, updated_at " +
        "FROM " + NOME_TABELA + " WHERE id = ?";
    
    private static final String SQL_BUSCAR_POR_NOME = 
        "SELECT id, name, description, price, category, stock_quantity, created_at, updated_at " +
        "FROM " + NOME_TABELA + " WHERE name = ?";
    
    private static final String SQL_BUSCAR_TODOS = 
        "SELECT id, name, description, price, category, stock_quantity, created_at, updated_at " +
        "FROM " + NOME_TABELA + " ORDER BY name";
    
    private static final String SQL_BUSCAR_POR_CATEGORIA = 
        "SELECT id, name, description, price, category, stock_quantity, created_at, updated_at " +
        "FROM " + NOME_TABELA + " WHERE category = ? ORDER BY name";
    
    private static final String SQL_BUSCAR_SEM_ESTOQUE = 
        "SELECT id, name, description, price, category, stock_quantity, created_at, updated_at " +
        "FROM " + NOME_TABELA + " WHERE stock_quantity = 0 ORDER BY name";
    
    private static final String SQL_BUSCAR_ESTOQUE_BAIXO = 
        "SELECT id, name, description, price, category, stock_quantity, created_at, updated_at " +
        "FROM " + NOME_TABELA + " WHERE stock_quantity < ? ORDER BY stock_quantity, name";
    
    private static final String SQL_CONTAR = 
        "SELECT COUNT(*) FROM " + NOME_TABELA;
    
    private static final String SQL_EXISTE_POR_NOME = 
        "SELECT 1 FROM " + NOME_TABELA + " WHERE name = ? LIMIT 1";
    
    private static final String SQL_EXISTE_POR_ID = 
        "SELECT 1 FROM " + NOME_TABELA + " WHERE id = ? LIMIT 1";

    private static final Logger LOGGER = Logger.getLogger(RepositorioPostgreSqlProduto.class.getName());
    private final DataSource dataSource;

    public RepositorioPostgreSqlProduto(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Operações de comando
    
    @Override
    public void salvar(Produto produto) throws ExcecaoBancoDados {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERIR)) {
            
            definirParametrosProduto(stmt, produto);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro SQL ao salvar produto: " + e.getMessage(), e);
            String msgErro = "Falha ao salvar produto: " + produto.getId();
            if (e.getMessage() != null) {
                msgErro += " | SQL Error: " + e.getMessage();
            }
            throw new ExcecaoBancoDados("salvar", msgErro, e);
        }
    }

    @Override
    public void atualizar(Produto produto) throws ExcecaoBancoDados {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_ATUALIZAR)) {
            
            stmt.setString(1, produto.getNome().getValue());
            stmt.setString(2, produto.getDescricao().getValue());
            stmt.setBigDecimal(3, produto.getPreco().getValue());
            stmt.setString(4, produto.getCategoria().name());
            stmt.setInt(5, produto.getQuantidadeEstoque().getValue());
            stmt.setTimestamp(6, Timestamp.valueOf(produto.getAtualizadoEm()));
            stmt.setObject(7, produto.getId().getValue(), java.sql.Types.OTHER);
            
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new ExcecaoBancoDados("atualizar", "Produto não encontrado para atualização: " + produto.getId());
            }
            
        } catch (SQLException e) {
            throw new ExcecaoBancoDados("atualizar", "Falha ao atualizar produto: " + produto.getId(), e);
        }
    }

    @Override
    public void excluirPorId(ProdutoId produtoId) throws ExcecaoBancoDados {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_EXCLUIR_POR_ID)) {
            
            stmt.setObject(1, produtoId.getValue(), java.sql.Types.OTHER);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new ExcecaoBancoDados("excluir", "Falha ao excluir produto: " + produtoId, e);
        }
    }

    @Override
    public void excluirTodos() throws ExcecaoBancoDados {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_EXCLUIR_TODOS)) {
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new ExcecaoBancoDados("excluirTodos", "Falha ao excluir todos os produtos", e);
        }
    }

    // Operações de consulta
    
    @Override
    public Optional<Produto> buscarPorId(ProdutoId produtoId) throws ExcecaoBancoDados {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_BUSCAR_POR_ID)) {
            
            stmt.setObject(1, produtoId.getValue(), java.sql.Types.OTHER);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(criarProdutoDoResultSet(rs)) : Optional.empty();
            }
            
        } catch (SQLException e) {
            throw new ExcecaoBancoDados("buscarPorId", "Falha ao buscar produto por ID: " + produtoId, e);
        }
    }

    @Override
    public Optional<Produto> buscarPorNome(Nome nome) throws ExcecaoBancoDados {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_BUSCAR_POR_NOME)) {
            
            stmt.setString(1, nome.getValue());
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(criarProdutoDoResultSet(rs)) : Optional.empty();
            }
            
        } catch (SQLException e) {
            throw new ExcecaoBancoDados("buscarPorNome", "Falha ao buscar produto por nome: " + nome, e);
        }
    }

    @Override
    public List<Produto> buscarTodos() throws ExcecaoBancoDados {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_BUSCAR_TODOS);
             ResultSet rs = stmt.executeQuery()) {
            
            List<Produto> produtos = new ArrayList<>();
            while (rs.next()) {
                produtos.add(criarProdutoDoResultSet(rs));
            }
            return produtos;
            
        } catch (SQLException e) {
            throw new ExcecaoBancoDados("buscarTodos", "Falha ao buscar todos os produtos", e);
        }
    }

    @Override
    public List<Produto> buscarPorCategoria(Categoria categoria) throws ExcecaoBancoDados {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_BUSCAR_POR_CATEGORIA)) {
            
            stmt.setString(1, categoria.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Produto> produtos = new ArrayList<>();
                while (rs.next()) {
                    produtos.add(criarProdutoDoResultSet(rs));
                }
                return produtos;
            }
            
        } catch (SQLException e) {
            throw new ExcecaoBancoDados("buscarPorCategoria", "Falha ao buscar produtos por categoria: " + categoria, e);
        }
    }

    @Override
    public List<Produto> buscarProdutosSemEstoque() throws ExcecaoBancoDados {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_BUSCAR_SEM_ESTOQUE);
             ResultSet rs = stmt.executeQuery()) {
            
            List<Produto> produtos = new ArrayList<>();
            while (rs.next()) {
                produtos.add(criarProdutoDoResultSet(rs));
            }
            return produtos;
            
        } catch (SQLException e) {
            throw new ExcecaoBancoDados("buscarProdutosSemEstoque", "Falha ao buscar produtos sem estoque", e);
        }
    }

    @Override  
    public List<Produto> buscarProdutosEstoqueBaixo(int limite) throws ExcecaoBancoDados {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_BUSCAR_ESTOQUE_BAIXO)) {
            
            stmt.setInt(1, limite);
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Produto> produtos = new ArrayList<>();
                while (rs.next()) {
                    produtos.add(criarProdutoDoResultSet(rs));
                }
                return produtos;
            }
            
        } catch (SQLException e) {
            throw new ExcecaoBancoDados("buscarProdutosEstoqueBaixo", "Falha ao buscar produtos com estoque baixo", e);
        }
    }

    @Override
    public long contar() throws ExcecaoBancoDados {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_CONTAR);
             ResultSet rs = stmt.executeQuery()) {
            
            return rs.next() ? rs.getLong(1) : 0;
            
        } catch (SQLException e) {
            throw new ExcecaoBancoDados("contar", "Falha ao contar produtos", e);
        }
    }

    @Override
    public boolean existePorNome(Nome nome) throws ExcecaoBancoDados {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_EXISTE_POR_NOME)) {
            
            stmt.setString(1, nome.getValue());
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            throw new ExcecaoBancoDados("existePorNome", "Falha ao verificar se produto existe por nome: " + nome, e);
        }
    }

    @Override
    public boolean existePorId(ProdutoId produtoId) throws ExcecaoBancoDados {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_EXISTE_POR_ID)) {
            
            stmt.setObject(1, produtoId.getValue(), java.sql.Types.OTHER);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            throw new ExcecaoBancoDados("existePorId", "Falha ao verificar se produto existe por ID: " + produtoId, e);
        }
    }

    // Métodos auxiliares privados
    
    private void definirParametrosProduto(PreparedStatement stmt, Produto produto) throws SQLException {
        stmt.setObject(1, produto.getId().getValue(), java.sql.Types.OTHER);
        stmt.setString(2, produto.getNome().getValue());
        stmt.setString(3, produto.getDescricao().getValue());
        stmt.setBigDecimal(4, produto.getPreco().getValue());
        stmt.setString(5, produto.getCategoria().name());
        stmt.setInt(6, produto.getQuantidadeEstoque().getValue());
        stmt.setTimestamp(7, Timestamp.valueOf(produto.getCriadoEm()));
        stmt.setTimestamp(8, Timestamp.valueOf(produto.getAtualizadoEm()));
    }
    
    private Produto criarProdutoDoResultSet(ResultSet rs) throws SQLException {
        return Produto.builder()
                .id(ProdutoId.of((UUID) rs.getObject("id")))
                .nome(Nome.of(rs.getString("name")))
                .descricao(Descricao.of(rs.getString("description")))
                .preco(Preco.of(rs.getBigDecimal("price")))
                .categoria(Categoria.valueOf(rs.getString("category")))
                .quantidadeEstoque(QuantidadeEstoque.of(rs.getInt("stock_quantity")))
                .criadoEm(rs.getTimestamp("created_at").toLocalDateTime())
                .atualizadoEm(rs.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }
}