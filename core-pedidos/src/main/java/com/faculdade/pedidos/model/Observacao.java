package com.faculdade.pedidos.model;

import com.faculdade.pedidos.exception.ValidacaoException;

import java.util.Objects;

public final class Observacao {

    private static final int TAMANHO_MAXIMO = 255;

    private final String value;

    private Observacao(String value) {
        this.value = value;
    }

    public static Observacao ofNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalizado = value.trim();
        if (normalizado.isEmpty()) {
            return null;
        }
        if (normalizado.length() > TAMANHO_MAXIMO) {
            throw new ValidacaoException("Observação deve ter no máximo " + TAMANHO_MAXIMO + " caracteres.");
        }
        return new Observacao(normalizado);
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
        if (!(o instanceof Observacao that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
