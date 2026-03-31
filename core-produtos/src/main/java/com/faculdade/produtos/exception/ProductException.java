package com.faculdade.produtos.exception;

/**
 * Exceção base para todas as exceções de negócio do sistema de produtos.
 * Facilita o tratamento consistente de erros.
 */
public abstract class ProductException extends Exception {
    
    protected ProductException(String message) {
        super(message);
    }

    protected ProductException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Retorna código de erro específico para cada tipo de exceção.
     */
    public abstract String getErrorCode();

    /**
     * Retorna categoria do erro para logs e métricas.
     */
    public abstract ErrorCategory getErrorCategory();

    public enum ErrorCategory {
        VALIDATION,
        BUSINESS_RULE,
        INFRASTRUCTURE,
        EXTERNAL_SYSTEM
    }
}