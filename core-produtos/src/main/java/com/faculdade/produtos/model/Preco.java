package com.faculdade.produtos.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/*
    Value object para preço de produto.
    Garante que o preço é sempre positivo e com precisão adequada.
*/
public final class Preco {
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int ESCALA = 2; // Duas casas decimais
    
    private final BigDecimal value;

    private Preco(BigDecimal value) {
        this.value = validarENormalizar(value);
    }
    
    // Construtor interno para operações matemáticas que podem gerar valores negativos
    private Preco(BigDecimal value, boolean pularValidacao) {
        if (pularValidacao) {
            this.value = value.setScale(ESCALA, RoundingMode.HALF_UP);  
        } else {
            this.value = validarENormalizar(value);
        }
    }

    public static Preco of(BigDecimal value) {
        return new Preco(value);
    }

    public static Preco of(double value) {
        return new Preco(BigDecimal.valueOf(value));
    }

    public static Preco of(String value) {
        try {
            return new Preco(new BigDecimal(value));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato de preço inválido: " + value, e);
        }
    }

    public static Preco zero() {
        return new Preco(ZERO);
    }

    private BigDecimal validarENormalizar(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Preço não pode ser nulo");
        }
        
        if (value.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo");
        }
        
        return value.setScale(ESCALA, RoundingMode.HALF_UP);
    }

    public BigDecimal getValue() {
        return value;
    }

    public boolean ehZero() {
        return value.compareTo(ZERO) == 0;
    }

    public Preco somar(Preco outro) {
        return new Preco(this.value.add(outro.value));
    }

    public Preco subtrair(Preco outro) {
        return new Preco(this.value.subtract(outro.value), true);
    }

    public Preco multiplicar(BigDecimal fator) {
        return new Preco(this.value.multiply(fator));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Preco preco = (Preco) obj;
        return Objects.equals(value, preco.value);
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