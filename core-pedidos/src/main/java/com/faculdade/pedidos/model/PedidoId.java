package com.faculdade.pedidos.model;

import com.faculdade.pedidos.exception.ValidacaoException;

import java.util.Objects;
import java.util.UUID;

public final class PedidoId {

    private final String value;

    private PedidoId(String value) {
        this.value = value;
    }

    public static PedidoId gerar() {
        return new PedidoId(UUID.randomUUID().toString());
    }

    public static PedidoId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidacaoException("ID do pedido é obrigatório.");
        }
        String normalizado = value.trim();
        try {
            UUID.fromString(normalizado);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException("ID do pedido inválido.");
        }
        return new PedidoId(normalizado);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PedidoId pedidoId)) return false;
        return Objects.equals(value, pedidoId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
