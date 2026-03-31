package com.faculdade.produtos.model;

import java.util.Objects;

/*
    Value object para descrição de produto.
*/
public final class Descricao {
    private static final int COMPRIMENTO_MAXIMO = 500;
    
    private final String value;

    private Descricao(String value) {
        this.value = validarENormalizar(value);
    }

    public static Descricao of(String value) {
        return new Descricao(value);
    }

    private String validarENormalizar(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Descrição do produto não pode ser nula");
        }
        
        String normalizada = value.trim();
        
        if (normalizada.length() > COMPRIMENTO_MAXIMO) {
            throw new IllegalArgumentException(
                String.format("Descrição do produto não pode exceder %d caracteres", COMPRIMENTO_MAXIMO)
            );
        }
        
        return normalizada;
    }

    public String getValue() {
        return value;
    }

    public boolean estaVazia() {
        return value.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Descricao descricao = (Descricao) obj;
        return Objects.equals(value, descricao.value);
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