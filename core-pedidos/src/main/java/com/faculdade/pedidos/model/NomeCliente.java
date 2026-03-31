package com.faculdade.pedidos.model;

import com.faculdade.pedidos.exception.ValidacaoException;

import java.util.Objects;

public final class NomeCliente {

    private static final int TAMANHO_MAXIMO = 100;

    private final String value;

    private NomeCliente(String value) {
        this.value = value;
    }

    public static NomeCliente of(String value) {
        if (value == null) {
            throw new ValidacaoException("Nome do cliente é obrigatório.");
        }
        String normalizado = value.trim();
        if (normalizado.isEmpty()) {
            throw new ValidacaoException("Nome do cliente é obrigatório.");
        }
        if (normalizado.length() > TAMANHO_MAXIMO) {
            throw new ValidacaoException("Nome do cliente deve ter no máximo " + TAMANHO_MAXIMO + " caracteres.");
        }
        return new NomeCliente(normalizado);
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NomeCliente that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
