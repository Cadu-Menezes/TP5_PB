package com.faculdade.pedidos.model;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Pedido {

    private final PedidoId id;
    private final String produtoId;
    private final NomeCliente nomeCliente;
    private final Observacao observacao;
    private final StatusPedido status;
    private final LocalDateTime criadoEm;
    private final LocalDateTime atualizadoEm;

    private Pedido(PedidoId id,
                   String produtoId,
                   NomeCliente nomeCliente,
                   Observacao observacao,
                   StatusPedido status,
                   LocalDateTime criadoEm,
                   LocalDateTime atualizadoEm) {
        this.id = id;
        this.produtoId = normalizarProdutoId(produtoId);
        this.nomeCliente = nomeCliente;
        this.observacao = observacao;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static Pedido criar(NomeCliente nomeCliente, Observacao observacao, StatusPedido status) {
        return criar(nomeCliente, observacao, status, null);
    }

    public static Pedido criar(NomeCliente nomeCliente, Observacao observacao, StatusPedido status, String produtoId) {
        LocalDateTime agora = LocalDateTime.now();
        return new Pedido(PedidoId.gerar(), produtoId, nomeCliente, observacao, status, agora, agora);
    }

    public static Pedido reconstruir(PedidoId id,
                                     String produtoId,
                                     NomeCliente nomeCliente,
                                     Observacao observacao,
                                     StatusPedido status,
                                     LocalDateTime criadoEm,
                                     LocalDateTime atualizadoEm) {
        return new Pedido(id, produtoId, nomeCliente, observacao, status, criadoEm, atualizadoEm);
    }

    public Pedido atualizar(NomeCliente novoNomeCliente, Observacao novaObservacao, StatusPedido novoStatus) {
        return new Pedido(this.id, this.produtoId, novoNomeCliente, novaObservacao, novoStatus, this.criadoEm, LocalDateTime.now());
    }

    public PedidoId getId() {
        return id;
    }

    public NomeCliente getNomeCliente() {
        return nomeCliente;
    }

    public String getProdutoId() {
        return produtoId;
    }

    public Observacao getObservacao() {
        return observacao;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pedido pedido)) return false;
        return Objects.equals(id, pedido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static String normalizarProdutoId(String produtoId) {
        if (produtoId == null) {
            return null;
        }

        String normalizado = produtoId.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }
}
