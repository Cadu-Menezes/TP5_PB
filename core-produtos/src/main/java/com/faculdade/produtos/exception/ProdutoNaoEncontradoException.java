package com.faculdade.produtos.exception;

import com.faculdade.produtos.model.ProdutoId;

/**
 * Exceção lançada quando um produto não é encontrado.
 */
public final class ProdutoNaoEncontradoException extends ExcecaoProduto {
    private static final String CODIGO_ERRO = "PRODUTO_NAO_ENCONTRADO";
    
    private final ProdutoId produtoId;

    public ProdutoNaoEncontradoException(ProdutoId produtoId) {
        super(String.format("Produto com ID '%s' não encontrado", produtoId));
        this.produtoId = produtoId;
    }

    public ProdutoId getProdutoId() {
        return produtoId;
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