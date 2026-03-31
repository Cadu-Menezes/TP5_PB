package com.faculdade.produtos.exception;

import com.faculdade.produtos.model.ProdutoId;
import com.faculdade.produtos.model.QuantidadeEstoque;

/**
 * Exceção lançada quando não há estoque suficiente para uma operação.
 */
public final class EstoqueInsuficienteException extends ExcecaoProduto {
    private static final String CODIGO_ERRO = "ESTOQUE_INSUFICIENTE";
    
    private final ProdutoId produtoId;
    private final QuantidadeEstoque estoqueAtual;
    private final QuantidadeEstoque quantidadeSolicitada;

    public EstoqueInsuficienteException(ProdutoId produtoId, QuantidadeEstoque estoqueAtual, QuantidadeEstoque quantidadeSolicitada) {
        super(String.format("Estoque insuficiente para o produto '%s'. Disponível: %s, Solicitado: %s", 
                produtoId, estoqueAtual, quantidadeSolicitada));
        this.produtoId = produtoId;
        this.estoqueAtual = estoqueAtual;
        this.quantidadeSolicitada = quantidadeSolicitada;
    }

    public ProdutoId getProdutoId() {
        return produtoId;
    }

    public QuantidadeEstoque getEstoqueAtual() {
        return estoqueAtual;
    }

    public QuantidadeEstoque getQuantidadeSolicitada() {
        return quantidadeSolicitada;
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