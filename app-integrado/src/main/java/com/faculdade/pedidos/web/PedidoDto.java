package com.faculdade.pedidos.web;

import com.faculdade.pedidos.model.Pedido;

import java.time.LocalDateTime;

public record PedidoDto(
        String id,
    String produtoId,
        String nomeCliente,
        String observacao,
        String status,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static PedidoDto fromModel(Pedido pedido) {
        return new PedidoDto(
                pedido.getId().getValue(),
            pedido.getProdutoId(),
                pedido.getNomeCliente().getValue(),
                pedido.getObservacao() == null ? null : pedido.getObservacao().getValue(),
                pedido.getStatus().name(),
                pedido.getCriadoEm(),
                pedido.getAtualizadoEm()
        );
    }
}
