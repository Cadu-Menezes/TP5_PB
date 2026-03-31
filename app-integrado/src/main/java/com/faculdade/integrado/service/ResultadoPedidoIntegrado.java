package com.faculdade.integrado.service;

public record ResultadoPedidoIntegrado(
        String pedidoId,
        String produtoId,
        int estoqueAtual
) {
}
