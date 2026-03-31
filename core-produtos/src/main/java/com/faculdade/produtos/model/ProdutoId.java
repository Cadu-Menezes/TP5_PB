package com.faculdade.produtos.model;

import java.util.Objects;
import java.util.UUID;


public final class ProdutoId {
    private final UUID value;

    private ProdutoId(UUID value) {
        this.value = Objects.requireNonNull(value, "ID do produto não pode ser nulo");
    }

    public static ProdutoId of(UUID value) {
        return new ProdutoId(value);
    }

    public static ProdutoId of(String value) {
        try {
            return new ProdutoId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Formato de ID do produto inválido: " + value, e);
        }
    }

    public static ProdutoId gerar() {
        return new ProdutoId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProdutoId produtoId = (ProdutoId) obj;
        return Objects.equals(value, produtoId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}