package com.faculdade.produtos.exception;

/**
 * Exceção base para todas as exceções de negócio do sistema de produtos.
 * Facilita o tratamento consistente de erros.
 */
public abstract class ExcecaoProduto extends Exception {
    
    protected ExcecaoProduto(String message) {
        super(message);
    }

    protected ExcecaoProduto(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Retorna código de erro específico para cada tipo de exceção.
     */
    public abstract String obterCodigoErro();

    /**
     * Retorna categoria do erro para logs e métricas.
     */
    public abstract CategoriaErro obterCategoriaErro();

    public enum CategoriaErro {
        VALIDACAO,
        REGRA_NEGOCIO,
        INFRAESTRUTURA,
        SISTEMA_EXTERNO
    }
}