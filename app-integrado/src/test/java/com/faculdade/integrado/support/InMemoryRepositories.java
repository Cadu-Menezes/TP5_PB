package com.faculdade.integrado.support;

import com.faculdade.pedidos.model.Pedido;
import com.faculdade.pedidos.model.PedidoId;
import com.faculdade.pedidos.repository.PedidoCommandRepository;
import com.faculdade.pedidos.repository.PedidoQueryRepository;
import com.faculdade.produtos.exception.ExcecaoBancoDados;
import com.faculdade.produtos.model.Categoria;
import com.faculdade.produtos.model.Nome;
import com.faculdade.produtos.model.Produto;
import com.faculdade.produtos.model.ProdutoId;
import com.faculdade.produtos.repository.RepositorioComandoProduto;
import com.faculdade.produtos.repository.RepositorioConsultaProduto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryRepositories {

    private InMemoryRepositories() {
    }

    public static final class ProdutoRepository implements RepositorioComandoProduto, RepositorioConsultaProduto {
        private final Map<ProdutoId, Produto> storage = new HashMap<>();

        @Override
        public void salvar(Produto produto) throws ExcecaoBancoDados {
            if (existePorNome(produto.getNome())) {
                throw new ExcecaoBancoDados("salvar", "Falha ao salvar produto: duplicate key value violates unique constraint \"products_name_key\"");
            }
            storage.put(produto.getId(), produto);
        }

        @Override
        public void atualizar(Produto produto) {
            storage.put(produto.getId(), produto);
        }

        @Override
        public void excluirPorId(ProdutoId produtoId) {
            storage.remove(produtoId);
        }

        @Override
        public void excluirTodos() {
            storage.clear();
        }

        @Override
        public Optional<Produto> buscarPorId(ProdutoId produtoId) {
            return Optional.ofNullable(storage.get(produtoId));
        }

        @Override
        public Optional<Produto> buscarPorNome(Nome nome) {
            return storage.values().stream().filter(p -> p.getNome().equals(nome)).findFirst();
        }

        @Override
        public List<Produto> buscarTodos() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public List<Produto> buscarPorCategoria(Categoria categoria) {
            return storage.values().stream().filter(p -> p.getCategoria() == categoria).toList();
        }

        @Override
        public List<Produto> buscarProdutosSemEstoque() {
            return storage.values().stream().filter(p -> p.getQuantidadeEstoque().getValue() == 0).toList();
        }

        @Override
        public List<Produto> buscarProdutosEstoqueBaixo(int limite) {
            return storage.values().stream().filter(p -> p.getQuantidadeEstoque().getValue() <= limite).toList();
        }

        @Override
        public long contar() {
            return storage.size();
        }

        @Override
        public boolean existePorNome(Nome nome) {
            return storage.values().stream().anyMatch(p -> p.getNome().equals(nome));
        }

        @Override
        public boolean existePorId(ProdutoId produtoId) {
            return storage.containsKey(produtoId);
        }
    }

    public static final class PedidoRepository implements PedidoCommandRepository, PedidoQueryRepository {
        private final Map<PedidoId, Pedido> storage = new LinkedHashMap<>();
        private final boolean falharCriacao;

        public PedidoRepository(boolean falharCriacao) {
            this.falharCriacao = falharCriacao;
        }

        @Override
        public void criar(Pedido pedido) {
            if (falharCriacao) {
                throw new RuntimeException("Falha simulada ao criar pedido.");
            }
            storage.put(pedido.getId(), pedido);
        }

        @Override
        public void atualizar(Pedido pedido) {
            storage.put(pedido.getId(), pedido);
        }

        @Override
        public void excluir(PedidoId id) {
            storage.remove(id);
        }

        @Override
        public Optional<Pedido> buscarPorId(PedidoId id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<Pedido> listarTodos() {
            return new ArrayList<>(storage.values());
        }
    }
}