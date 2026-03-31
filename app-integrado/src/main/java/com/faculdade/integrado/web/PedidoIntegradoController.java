package com.faculdade.integrado.web;

import com.faculdade.integrado.service.PedidoEstoqueIntegracaoService;
import com.faculdade.integrado.service.ResultadoPedidoIntegrado;
import com.faculdade.produtos.exception.EstoqueInsuficienteException;
import com.faculdade.produtos.exception.ExcecaoBancoDados;
import com.faculdade.produtos.exception.ProdutoNaoEncontradoException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public final class PedidoIntegradoController {

    private final PedidoEstoqueIntegracaoService integracaoService;

    public PedidoIntegradoController(PedidoEstoqueIntegracaoService integracaoService) {
        this.integracaoService = integracaoService;
    }

    public void criarComBaixaEstoque(Context ctx) {
        try {
            PedidoIntegradoRequest request = ctx.bodyAsClass(PedidoIntegradoRequest.class);
            int quantidade = request.quantidade() == null ? 0 : request.quantidade();

            ResultadoPedidoIntegrado resultado = integracaoService.criarPedidoComBaixaEstoque(
                    request.produtoId(),
                    quantidade,
                    request.nomeCliente(),
                    request.observacao(),
                    request.status()
            );

            ctx.status(HttpStatus.CREATED);
            ctx.json(resultado);
        } catch (ProdutoNaoEncontradoException e) {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.json(new ErrorResponse(e.getMessage()));
        } catch (EstoqueInsuficienteException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new ErrorResponse(e.getMessage()));
        } catch (ExcecaoBancoDados e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(new ErrorResponse(e.getMessage()));
        }
    }

    private record ErrorResponse(String mensagem) {
    }
}
