package com.faculdade.produtos.repository;

import com.faculdade.produtos.model.Produto;
import com.faculdade.produtos.model.ProdutoId;
import com.faculdade.produtos.model.Nome;
import com.faculdade.produtos.model.Categoria;
import com.faculdade.produtos.exception.ExcecaoBancoDados;

import java.util.List;
import java.util.Optional;

/*
    Interface para consultas (operações que não modificam o estado).
    Implementa princípio Command Query Separation (CQS).
*/
public interface RepositorioConsultaProduto {

    /**
        Busca produto por ID.
      
        @param produtoId ID do produto
        @return Optional contendo o produto se encontrado
        @throws ExcecaoBancoDados se houver erro na operação de banco
    */
    Optional<Produto> buscarPorId(ProdutoId produtoId) throws ExcecaoBancoDados;

    /**
        Busca produto por nome.
      
        @param nome nome do produto
        @return Optional contendo o produto se encontrado
        @throws ExcecaoBancoDados se houver erro na operação de banco
    */
    Optional<Produto> buscarPorNome(Nome nome) throws ExcecaoBancoDados;

    /**
        Lista todos os produtos.
      
        @return lista de todos os produtos
        @throws ExcecaoBancoDados se houver erro na operação de banco
    */
    List<Produto> buscarTodos() throws ExcecaoBancoDados;

    /**
        Lista produtos por categoria.
      
        @param categoria categoria dos produtos
        @return lista de produtos da categoria especificada
        @throws ExcecaoBancoDados se houver erro na operação de banco
    */
    List<Produto> buscarPorCategoria(Categoria categoria) throws ExcecaoBancoDados;

    /**
        Lista produtos com estoque zero.
      
        @return lista de produtos sem estoque
        @throws ExcecaoBancoDados se houver erro na operação de banco
    */
    List<Produto> buscarProdutosSemEstoque() throws ExcecaoBancoDados;

    /**
        Lista produtos com estoque baixo (menos que a quantidade especificada).
      
        @param limite limite mínimo de estoque
        @return lista de produtos com estoque baixo
        @throws ExcecaoBancoDados se houver erro na operação de banco
    */
    List<Produto> buscarProdutosEstoqueBaixo(int limite) throws ExcecaoBancoDados;

    /**
        Conta o total de produtos.
      
        @return número total de produtos
        @throws ExcecaoBancoDados se houver erro na operação de banco
    */
    long contar() throws ExcecaoBancoDados;

    /**
        Verifica se existe produto com o nome especificado.
      
        @param nome nome a verificar
        @return true se existe produto com o nome
        @throws ExcecaoBancoDados se houver erro na operação de banco
    */
    boolean existePorNome(Nome nome) throws ExcecaoBancoDados;

    /**
        Verifica se existe produto com o ID especificado.
      
        @param produtoId ID a verificar
        @return true se existe produto com o ID
        @throws ExcecaoBancoDados se houver erro na operação de banco
    */
    boolean existePorId(ProdutoId produtoId) throws ExcecaoBancoDados;
}