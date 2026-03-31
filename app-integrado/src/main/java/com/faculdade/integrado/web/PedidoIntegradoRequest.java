package com.faculdade.integrado.web;

public record PedidoIntegradoRequest(
        String produtoId,
        Integer quantidade,
        String nomeCliente,
        String observacao,
        String status
) {
}
