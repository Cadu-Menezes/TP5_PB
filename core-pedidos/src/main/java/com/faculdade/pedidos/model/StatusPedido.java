package com.faculdade.pedidos.model;

import com.faculdade.pedidos.exception.ValidacaoException;

public enum StatusPedido {
    ABERTO,
    EM_ANDAMENTO,
    CONCLUIDO,
    CANCELADO;

    public static StatusPedido fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ABERTO;
        }
        try {
            return StatusPedido.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException("Status inválido. Use: ABERTO, EM_ANDAMENTO, CONCLUIDO ou CANCELADO.");
        }
    }
}
