package com.faculdade.produtos.model;

import java.util.Objects;

public final class QuantidadeEstoque {
    private static final int QUANTIDADE_MINIMA = 0;
    
    private final int value;

    private QuantidadeEstoque(int value) {
        this.value = validar(value);
    }

    public static QuantidadeEstoque of(int value) {
        return new QuantidadeEstoque(value);
    }

    public static QuantidadeEstoque zero() {
        return new QuantidadeEstoque(QUANTIDADE_MINIMA);
    }

    private int validar(int value) {
        if (value < QUANTIDADE_MINIMA) {
            throw new IllegalArgumentException(
                String.format("Quantidade em estoque não pode ser negativa. Valor informado: %d", value)
            );
        }
        return value;
    }

    public int getValue() {
        return value;
    }

    public boolean ehZero() {
        return value == QUANTIDADE_MINIMA;
    }

    public boolean estaDisponivel() {
        return value > QUANTIDADE_MINIMA;
    }

    public QuantidadeEstoque adicionar(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("Não é possível adicionar quantidade negativa");
        }
        return new QuantidadeEstoque(this.value + quantidade);
    }

    public QuantidadeEstoque subtrair(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("Não é possível subtrair quantidade negativa");
        }
        return new QuantidadeEstoque(this.value - quantidade);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        QuantidadeEstoque that = (QuantidadeEstoque) obj;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}