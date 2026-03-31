package com.faculdade.integrado.service;

import com.faculdade.pedidos.model.Pedido;
import com.faculdade.pedidos.service.PedidoService;
import com.faculdade.produtos.exception.EstoqueInsuficienteException;
import com.faculdade.produtos.exception.ExcecaoBancoDados;
import com.faculdade.produtos.exception.ProdutoNaoEncontradoException;
import com.faculdade.produtos.model.Produto;
import com.faculdade.produtos.model.ProdutoId;
import com.faculdade.produtos.model.QuantidadeEstoque;
import com.faculdade.produtos.service.ProdutoService;

public final class PedidoEstoqueIntegracaoService {

    private final ProdutoService produtoService;
    private final PedidoService pedidoService;

    public PedidoEstoqueIntegracaoService(ProdutoService produtoService, PedidoService pedidoService) {
        this.produtoService = produtoService;
        this.pedidoService = pedidoService;
    }

    public ResultadoPedidoIntegrado criarPedidoComBaixaEstoque(
            String produtoId,
            int quantidade,
            String nomeCliente,
            String observacao,
            String status
        ) throws ProdutoNaoEncontradoException, ExcecaoBancoDados, EstoqueInsuficienteException {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }

        ProdutoId idProduto = ProdutoId.of(produtoId);
        QuantidadeEstoque quantidadeSolicitada = QuantidadeEstoque.of(quantidade);

        produtoService.reduzirEstoque(idProduto, quantidadeSolicitada);

        try {
            Pedido pedido = pedidoService.criar(nomeCliente, observacao, status, produtoId);
            Produto produtoAtualizado = produtoService.obterProduto(idProduto);
            return new ResultadoPedidoIntegrado(
                    pedido.getId().getValue(),
                    produtoAtualizado.getId().toString(),
                    produtoAtualizado.getQuantidadeEstoque().getValue()
            );
        } catch (RuntimeException e) {
            try {
                produtoService.aumentarEstoque(idProduto, quantidadeSolicitada);
            } catch (Exception rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;
        }
    }
}
