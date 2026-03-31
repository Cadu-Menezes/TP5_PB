package com.faculdade.integrado.service;

import com.faculdade.pedidos.service.PedidoService;
import com.faculdade.integrado.support.InMemoryRepositories;
import com.faculdade.produtos.exception.EstoqueInsuficienteException;
import com.faculdade.produtos.model.*;
import com.faculdade.produtos.service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PedidoEstoqueIntegracaoServiceTest {

    private ProdutoService produtoService;

    @BeforeEach
    void setUp() {
        InMemoryRepositories.ProdutoRepository produtoRepository = new InMemoryRepositories.ProdutoRepository();
        this.produtoService = new ProdutoService(produtoRepository, produtoRepository);
    }

    @Test
    void deveCriarPedidoEReduzirEstoque() throws Exception {
        ProdutoId produtoId = produtoService.criarProduto(
                Nome.of("Notebook X"),
                Descricao.of("Notebook para teste"),
                Preco.of(new BigDecimal("3500.00")),
                Categoria.ELETRONICOS,
                QuantidadeEstoque.of(10)
        );

        InMemoryRepositories.PedidoRepository pedidoRepository = new InMemoryRepositories.PedidoRepository(false);
        PedidoService pedidoService = new PedidoService(pedidoRepository, pedidoRepository);
        PedidoEstoqueIntegracaoService integracaoService = new PedidoEstoqueIntegracaoService(produtoService, pedidoService);

        ResultadoPedidoIntegrado resultado = integracaoService.criarPedidoComBaixaEstoque(
                produtoId.toString(),
                3,
                "Cliente A",
                "Entrega normal",
                "ABERTO"
        );

        assertNotNull(resultado.pedidoId());
        assertEquals(produtoId.toString(), resultado.produtoId());
        assertEquals(7, resultado.estoqueAtual());
        assertEquals(1, pedidoRepository.listarTodos().size());
    }

    @Test
    void deveFalharQuandoEstoqueInsuficiente() throws Exception {
        ProdutoId produtoId = produtoService.criarProduto(
                Nome.of("Mouse Gamer"),
                Descricao.of("Mouse para teste"),
                Preco.of(new BigDecimal("199.90")),
                Categoria.ELETRONICOS,
                QuantidadeEstoque.of(2)
        );

        InMemoryRepositories.PedidoRepository pedidoRepository = new InMemoryRepositories.PedidoRepository(false);
        PedidoService pedidoService = new PedidoService(pedidoRepository, pedidoRepository);
        PedidoEstoqueIntegracaoService integracaoService = new PedidoEstoqueIntegracaoService(produtoService, pedidoService);

        assertThrows(EstoqueInsuficienteException.class, () -> integracaoService.criarPedidoComBaixaEstoque(
                produtoId.toString(),
                5,
                "Cliente B",
                null,
                "ABERTO"
        ));

        assertEquals(0, pedidoRepository.listarTodos().size());
        assertEquals(2, produtoService.obterProduto(produtoId).getQuantidadeEstoque().getValue());
    }

    @Test
    void deveFazerRollbackDoEstoqueQuandoCriacaoPedidoFalha() throws Exception {
        ProdutoId produtoId = produtoService.criarProduto(
                Nome.of("Teclado Mecanico"),
                Descricao.of("Teclado para teste"),
                Preco.of(new BigDecimal("450.00")),
                Categoria.ELETRONICOS,
                QuantidadeEstoque.of(8)
        );

        InMemoryRepositories.PedidoRepository pedidoRepository = new InMemoryRepositories.PedidoRepository(true);
        PedidoService pedidoService = new PedidoService(pedidoRepository, pedidoRepository);
        PedidoEstoqueIntegracaoService integracaoService = new PedidoEstoqueIntegracaoService(produtoService, pedidoService);

        assertThrows(RuntimeException.class, () -> integracaoService.criarPedidoComBaixaEstoque(
                produtoId.toString(),
                4,
                "Cliente C",
                "Pedido com falha simulada",
                "ABERTO"
        ));

        assertEquals(8, produtoService.obterProduto(produtoId).getQuantidadeEstoque().getValue());
    }

}
