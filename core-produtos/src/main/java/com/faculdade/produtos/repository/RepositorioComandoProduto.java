package com.faculdade.produtos.repository;

import com.faculdade.produtos.model.Produto;
import com.faculdade.produtos.model.ProdutoId;
import com.faculdade.produtos.exception.ExcecaoBancoDados;

/*
    Interface para comandos (operações que modificam o estado).
    Implementa princípio Command Query Separation (CQS).
*/
public interface RepositorioComandoProduto {

    /**
        Salva um novo produto no repositório.
      
        @param produto produto a ser salvo
        @throws ExcecaoBancoDados se houver erro na operação de banco
    */
    void salvar(Produto produto) throws ExcecaoBancoDados;

    /**
        Atualiza um produto existente.
      
        @param produto produto com dados atualizados
        @throws ExcecaoBancoDados se houver erro na operação de banco
    */
    void atualizar(Produto produto) throws ExcecaoBancoDados;

    /**
        Remove um produto do repositório.
      
        @param produtoId ID do produto a ser removido
        @throws ExcecaoBancoDados se houver erro na operação de banco
    */
    void excluirPorId(ProdutoId produtoId) throws ExcecaoBancoDados;

    /**
        Remove todos os produtos do repositório.
        Usado principalmente para limpeza de testes.
      
        @throws ExcecaoBancoDados se houver erro na operação de banco
    */
    void excluirTodos() throws ExcecaoBancoDados;
}