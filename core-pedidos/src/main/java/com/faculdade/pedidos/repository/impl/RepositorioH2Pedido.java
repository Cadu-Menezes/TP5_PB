package com.faculdade.pedidos.repository.impl;

import com.faculdade.pedidos.exception.InfraestruturaException;
import com.faculdade.pedidos.model.*;
import com.faculdade.pedidos.repository.PedidoCommandRepository;
import com.faculdade.pedidos.repository.PedidoQueryRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RepositorioH2Pedido implements PedidoCommandRepository, PedidoQueryRepository {

    private final DataSource dataSource;

    public RepositorioH2Pedido(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void criar(Pedido pedido) {
        String sql = """
            INSERT INTO pedidos (id, produto_id, nome_cliente, observacao, status, criado_em, atualizado_em)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pedido.getId().getValue());
            if (pedido.getProdutoId() == null) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, pedido.getProdutoId());
            }
            ps.setString(3, pedido.getNomeCliente().getValue());
            if (pedido.getObservacao() == null) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, pedido.getObservacao().getValue());
            }
            ps.setString(5, pedido.getStatus().name());
            ps.setTimestamp(6, Timestamp.valueOf(pedido.getCriadoEm()));
            ps.setTimestamp(7, Timestamp.valueOf(pedido.getAtualizadoEm()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new InfraestruturaException("Falha ao criar pedido.", e);
        }
    }

    @Override
    public void atualizar(Pedido pedido) {
        String sql = """
            UPDATE pedidos
               SET produto_id = ?, nome_cliente = ?, observacao = ?, status = ?, atualizado_em = ?
             WHERE id = ?
        """;

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (pedido.getProdutoId() == null) {
                ps.setNull(1, Types.VARCHAR);
            } else {
                ps.setString(1, pedido.getProdutoId());
            }
            ps.setString(2, pedido.getNomeCliente().getValue());
            if (pedido.getObservacao() == null) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, pedido.getObservacao().getValue());
            }
            ps.setString(4, pedido.getStatus().name());
            ps.setTimestamp(5, Timestamp.valueOf(pedido.getAtualizadoEm()));
            ps.setString(6, pedido.getId().getValue());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new InfraestruturaException("Falha ao atualizar pedido.", e);
        }
    }

    @Override
    public void excluir(PedidoId id) {
        String sql = "DELETE FROM pedidos WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.getValue());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new InfraestruturaException("Falha ao excluir pedido.", e);
        }
    }

    @Override
    public Optional<Pedido> buscarPorId(PedidoId id) {
        String sql = "SELECT id, produto_id, nome_cliente, observacao, status, criado_em, atualizado_em FROM pedidos WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapperPedido(rs));
            }
        } catch (SQLException e) {
            throw new InfraestruturaException("Falha ao buscar pedido.", e);
        }
    }

    @Override
    public List<Pedido> listarTodos() {
        String sql = "SELECT id, produto_id, nome_cliente, observacao, status, criado_em, atualizado_em FROM pedidos ORDER BY atualizado_em DESC";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<Pedido> pedidos = new ArrayList<>();
            while (rs.next()) {
                pedidos.add(mapperPedido(rs));
            }
            return pedidos;
        } catch (SQLException e) {
            throw new InfraestruturaException("Falha ao listar pedidos.", e);
        }
    }

    private Pedido mapperPedido(ResultSet rs) throws SQLException {
        PedidoId id = PedidoId.of(rs.getString("id"));
        String produtoId = rs.getString("produto_id");
        NomeCliente nomeCliente = NomeCliente.of(rs.getString("nome_cliente"));
        Observacao observacao = Observacao.ofNullable(rs.getString("observacao"));
        StatusPedido status = StatusPedido.fromString(rs.getString("status"));
        LocalDateTime criadoEm = rs.getTimestamp("criado_em").toLocalDateTime();
        LocalDateTime atualizadoEm = rs.getTimestamp("atualizado_em").toLocalDateTime();
        return Pedido.reconstruir(id, produtoId, nomeCliente, observacao, status, criadoEm, atualizadoEm);
    }
}
