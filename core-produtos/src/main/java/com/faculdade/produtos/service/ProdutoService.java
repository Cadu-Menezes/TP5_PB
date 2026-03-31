package com.faculdade.produtos.service;

import com.faculdade.produtos.exception.*;
import com.faculdade.produtos.model.*;
import com.faculdade.produtos.repository.RepositorioComandoProduto;
import com.faculdade.produtos.repository.RepositorioConsultaProduto;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/*
    Serviço para gerenciamento de produtos.
    regras de negócio e coordena operações entre repositories.
*/
public final class ProdutoService {
    
    private final RepositorioComandoProduto repositorioComando;
    private final RepositorioConsultaProduto repositorioConsulta;

    public ProdutoService(RepositorioComandoProduto repositorioComando, 
                         RepositorioConsultaProduto repositorioConsulta) {
        this.repositorioComando = Objects.requireNonNull(repositorioComando, "Repositório de comando não pode ser nulo");
        this.repositorioConsulta = Objects.requireNonNull(repositorioConsulta, "Repositório de consulta não pode ser nulo");
    }

    // Command operations (CQS - Commands)

    /*
        Cria um novo produto.
        Valida regras de negócio antes da criação.
    */
    public ProdutoId criarProduto(Nome nome, Descricao descricao, Preco preco, 
                                 Categoria categoria, QuantidadeEstoque quantidadeEstoque) 
            throws ProdutoJaExisteException, ExcecaoBancoDados {
        
        validarRegrasDeNegocio(nome, descricao, preco, quantidadeEstoque);
        
        Produto produto = Produto.criar(nome, descricao, preco, categoria, quantidadeEstoque);
        try {
            repositorioComando.salvar(produto);
        } catch (ExcecaoBancoDados e) {
            // A validação de nome único acima pode sofrer condição de corrida;
            // o índice único do banco é a proteção final.
            if (ehErroDeNomeDuplicado(e)) {
                throw new ProdutoJaExisteException(nome);
            }
            throw e;
        }
        
        return produto.getId();
    }

    /*
        Atualiza um produto existente.
        Valida regras de negócio e existência do produto.
    */
    public void atualizarProduto(ProdutoId produtoId, Nome nome, Descricao descricao, 
                            Preco preco, Categoria categoria, QuantidadeEstoque quantidadeEstoque) 
            throws ProdutoNaoEncontradoException, ProdutoJaExisteException, ExcecaoBancoDados {
        
        validarRegrasDeNegocio(nome, descricao, preco, quantidadeEstoque);
        
        Produto produtoExistente = encontrarProdutoPorId(produtoId);
        
        // Regra de negócio: nome único (exceto para o próprio produto)
        if (!produtoExistente.getNome().equals(nome) && repositorioConsulta.existePorNome(nome)) {
            throw new ProdutoJaExisteException(nome);
        }
        
        Produto produtoAtualizado = produtoExistente.atualizar(nome, descricao, preco, categoria, quantidadeEstoque);
        repositorioComando.atualizar(produtoAtualizado);
    }

    
    //    Remove um produto do sistema.
    public void excluirProduto(ProdutoId produtoId) throws ProdutoNaoEncontradoException, ExcecaoBancoDados {
        // Verifica se produto existe antes de deletar
        if (!repositorioConsulta.existePorId(produtoId)) {
            throw new ProdutoNaoEncontradoException(produtoId);
        }
        
        repositorioComando.excluirPorId(produtoId);
    }

    /*
        Atualiza apenas o estoque de um produto.
        Operação específica para gerenciamento de estoque.
    */
    public void atualizarEstoque(ProdutoId produtoId, QuantidadeEstoque novaQuantidade) 
            throws ProdutoNaoEncontradoException, ExcecaoBancoDados {
        
        if (novaQuantidade == null) {
            throw new IllegalArgumentException("Quantidade em estoque não pode ser nula");
        }
        
        Produto produtoExistente = encontrarProdutoPorId(produtoId);
        Produto produtoAtualizado = produtoExistente.atualizarEstoque(novaQuantidade);
        repositorioComando.atualizar(produtoAtualizado);
    }

    
    //    Reduz estoque de um produto (para simulação de venda).
    public void reduzirEstoque(ProdutoId produtoId, QuantidadeEstoque quantidadeAReduzir) 
            throws ProdutoNaoEncontradoException, EstoqueInsuficienteException, ExcecaoBancoDados {
        
        if (quantidadeAReduzir == null) {
            throw new IllegalArgumentException("Quantidade a reduzir não pode ser nula");
        }
        
        Produto produtoExistente = encontrarProdutoPorId(produtoId);
        
        // Regra de negócio: verificar estoque suficiente
        if (produtoExistente.getQuantidadeEstoque().getValue() < quantidadeAReduzir.getValue()) {
            throw new EstoqueInsuficienteException(produtoId, produtoExistente.getQuantidadeEstoque(), quantidadeAReduzir);
        }
        
        QuantidadeEstoque novaQuantidade = produtoExistente.getQuantidadeEstoque().subtrair(quantidadeAReduzir.getValue());
        Produto produtoAtualizado = produtoExistente.atualizarEstoque(novaQuantidade);
        repositorioComando.atualizar(produtoAtualizado);
    }

    
    //    Aumenta estoque de um produto (para reposição).
    public void aumentarEstoque(ProdutoId produtoId, QuantidadeEstoque quantidadeAAdicionar) 
            throws ProdutoNaoEncontradoException, ExcecaoBancoDados {
        
        if (quantidadeAAdicionar == null) {
            throw new IllegalArgumentException("Quantidade a adicionar não pode ser nula");
        }
        
        Produto produtoExistente = encontrarProdutoPorId(produtoId);
        QuantidadeEstoque novaQuantidade = produtoExistente.getQuantidadeEstoque().adicionar(quantidadeAAdicionar.getValue());
        Produto produtoAtualizado = produtoExistente.atualizarEstoque(novaQuantidade);
        repositorioComando.atualizar(produtoAtualizado);
    }

    // Query operations (CQS - Queries)

    
    //Busca produto por ID.
    public Produto obterProduto(ProdutoId produtoId) throws ProdutoNaoEncontradoException, ExcecaoBancoDados {
        return encontrarProdutoPorId(produtoId);
    }


    //Busca produto por nome.    
    public Produto obterProduto(Nome nome) throws ProdutoNaoEncontradoException, ExcecaoBancoDados {
        return repositorioConsulta.buscarPorNome(nome)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(ProdutoId.gerar())); // Hack: ProdutoNaoEncontradoException deveria aceitar nome
    }

    
    // Lista todos os produtos.
    public List<Produto> obterTodosProdutos() throws ExcecaoBancoDados {
        return repositorioConsulta.buscarTodos();
    }

    
    // Lista produtos por categoria.
    public List<Produto> obterProdutosPorCategoria(Categoria categoria) throws ExcecaoBancoDados {
        if (categoria == null) {
            throw new IllegalArgumentException("Categoria não pode ser nula");
        }
        return repositorioConsulta.buscarPorCategoria(categoria);
    }

    
    // Lista produtos sem estoque. 
    public List<Produto> obterProdutosSemEstoque() throws ExcecaoBancoDados {
        return repositorioConsulta.buscarProdutosSemEstoque();
    }

    
    // Lista produtos com estoque baixo.
    public List<Produto> obterProdutosComEstoqueBaixo(int limite) throws ExcecaoBancoDados {
        if (limite < 0) {
            throw new IllegalArgumentException("Limite deve ser não-negativo");
        }
        return repositorioConsulta.buscarProdutosEstoqueBaixo(limite);
    }

 
    // Conta total de produtos.
 
    public long obterTotalDeProdutos() throws ExcecaoBancoDados {
        return repositorioConsulta.contar();
    }

    
    // Verifica se produto existe pelo nome.
    public boolean produtoExistePorNome(Nome nome) throws ExcecaoBancoDados {
        if (nome == null) {
            throw new IllegalArgumentException("Nome não pode ser nulo");
        }
        return repositorioConsulta.existePorNome(nome);
    }

    // Helper methods

    private Produto encontrarProdutoPorId(ProdutoId produtoId) throws ProdutoNaoEncontradoException, ExcecaoBancoDados {
        if (produtoId == null) {
            throw new IllegalArgumentException("ID do produto não pode ser nulo");
        }
        
        return repositorioConsulta.buscarPorId(produtoId)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(produtoId));
    }

    private void validarRegrasDeNegocio(Nome nome, Descricao descricao, Preco preco, QuantidadeEstoque quantidadeEstoque) {
        if (nome == null) {
            throw new IllegalArgumentException("Nome do produto não pode ser nulo");
        }
        if (descricao == null) {
            throw new IllegalArgumentException("Descrição do produto não pode ser nula");
        }
        if (preco == null) {
            throw new IllegalArgumentException("Preço do produto não pode ser nulo");
        }
        if (quantidadeEstoque == null) {
            throw new IllegalArgumentException("Quantidade em estoque do produto não pode ser nula");
        }
        
        // Regras de negócio específicas podem ser adicionadas aqui
        // Por exemplo: preço mínimo, categorias permitidas, etc.
    }

    private boolean ehErroDeNomeDuplicado(ExcecaoBancoDados excecao) {
        final String sqlStateViolacaoUnica = "23505";
        final String constraintNomeProduto = "products_name_key";

        Throwable causa = excecao;
        while (causa != null) {
            if (causa instanceof SQLException sqlException) {
                if (sqlStateViolacaoUnica.equals(sqlException.getSQLState())) {
                    String mensagem = sqlException.getMessage();
                    if (mensagem != null && mensagem.contains(constraintNomeProduto)) {
                        return true;
                    }
                }
            }

            String mensagem = causa.getMessage();
            if (mensagem != null && mensagem.contains(constraintNomeProduto)) {
                return true;
            }

            causa = causa.getCause();
        }

        return false;
    }
}