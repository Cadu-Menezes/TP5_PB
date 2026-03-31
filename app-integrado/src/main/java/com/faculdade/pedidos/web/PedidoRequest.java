package com.faculdade.pedidos.web;

public record PedidoRequest(
        String nomeCliente,
        String observacao,
        String status
) {
}
