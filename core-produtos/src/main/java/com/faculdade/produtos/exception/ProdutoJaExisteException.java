package com.faculdade.produtos.exception;

import com.faculdade.produtos.model.Nome;

/**
 * Exceção lançada quando tenta-se criar um produto com nome já existente.
 */
public final class ProdutoJaExisteException extends ExcecaoProduto {
    private static final String CODIGO_ERRO = "PRODUTO_JA_EXISTE";
    
    private final Nome nomeProduto;

    public ProdutoJaExisteException(Nome nomeProduto) {
        super(String.format("Produto com nome '%s' já existe", nomeProduto));
        this.nomeProduto = nomeProduto;
    }

    public Nome getNomeProduto() {
        return nomeProduto;
    }

    @Override
    public String obterCodigoErro() {
        return CODIGO_ERRO;
    }

    @Override
    public CategoriaErro obterCategoriaErro() {
        return CategoriaErro.REGRA_NEGOCIO;
    }
}