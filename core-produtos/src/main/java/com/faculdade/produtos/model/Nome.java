package com.faculdade.produtos.model;

import java.util.Objects;

/*
    Value object para nome de produto.
    Garante que o nome sempre está em formato válido.
*/
public final class Nome {
    private static final int COMPRIMENTO_MINIMO = 1;
    private static final int COMPRIMENTO_MAXIMO = 100;
    
    private final String value;

    private Nome(String value) {
        this.value = validarENormalizar(value);
    }

    public static Nome of(String value) {
        return new Nome(value);
    }

    private String validarENormalizar(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Nome do produto não pode ser nulo");
        }
        
        // Verificar se é apenas espaços em branco antes do trim
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto não pode estar vazio");
        }
        
        String normalizado = value.trim();
        
        if (normalizado.length() < COMPRIMENTO_MINIMO) {
            throw new IllegalArgumentException(
                String.format("Nome do produto deve ter pelo menos %d caractere", COMPRIMENTO_MINIMO)
            );
        }
        
        if (normalizado.length() > COMPRIMENTO_MAXIMO) {
            throw new IllegalArgumentException(
                String.format("Nome do produto não pode exceder %d caracteres", COMPRIMENTO_MAXIMO)
            );
        }
        
        return normalizado;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Nome nome = (Nome) obj;
        return Objects.equals(value, nome.value);
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