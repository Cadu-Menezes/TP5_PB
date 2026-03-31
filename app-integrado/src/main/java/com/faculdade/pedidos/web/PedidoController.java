package com.faculdade.pedidos.web;

import com.faculdade.pedidos.exception.NaoEncontradoException;
import com.faculdade.pedidos.exception.ValidacaoException;
import com.faculdade.pedidos.model.Pedido;
import com.faculdade.pedidos.service.PedidoService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
    Controller REST para operações de pedido.
    Converte requisições HTTP em chamadas ao PedidoService
    e formata as respostas em JSON.
*/
public final class PedidoController {

    private final PedidoService pedidoService;
    private final Gson gson;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /*
        Lista todos os pedidos cadastrados.
        GET /api/pedidos
    */
    public void listar(Context ctx) {
        try {
            List<Pedido> pedidos = pedidoService.listarTodos();
            List<Map<String, Object>> resposta = pedidos.stream()
                    .map(this::pedidoParaMap)
                    .collect(Collectors.toList());

            ctx.json(resposta);
        } catch (Exception e) {
            System.err.println("ERRO AO LISTAR PEDIDOS: " + e.getMessage());
            e.printStackTrace();
            responderErro(ctx, HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao listar pedidos", e.getMessage());
        }
    }

    /*
        Cria um novo pedido.
        POST /api/pedidos
        Body: { nomeCliente, observacao, status }
    */
    public void criar(Context ctx) {
        try {
            PedidoRequest request = ctx.bodyAsClass(PedidoRequest.class);

            // Validação de campos obrigatórios
            if (request.nomeCliente() == null || request.nomeCliente().trim().isEmpty()) {
                responderErro(ctx, HttpStatus.BAD_REQUEST, "Dados inválidos", "Campo 'nomeCliente' é obrigatório");
                return;
            }

            String status = request.status() != null ? request.status() : "ABERTO";

            Pedido pedido = pedidoService.criar(request.nomeCliente(), request.observacao(), status);

            Map<String, Object> resposta = new HashMap<>();
            resposta.put("id", pedido.getId().getValue());
            resposta.put("mensagem", "Pedido criado com sucesso");

            ctx.status(HttpStatus.CREATED).json(resposta);

        } catch (ValidacaoException e) {
            responderErro(ctx, HttpStatus.BAD_REQUEST, "Dados inválidos", e.getMessage());
        } catch (IllegalArgumentException e) {
            responderErro(ctx, HttpStatus.BAD_REQUEST, "Dados inválidos", e.getMessage());
        } catch (Exception e) {
            System.err.println("ERRO NÃO CAPTURADO EM criar: " + e.getMessage());
            e.printStackTrace();
            responderErro(ctx, HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado", e.getMessage() != null ? e.getMessage() : "Erro desconhecido");
        }
    }

    /*
        Atualiza um pedido existente.
        PUT /api/pedidos/{id}
        Body: { nomeCliente, observacao, status }
    */
    public void atualizar(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            PedidoRequest request = ctx.bodyAsClass(PedidoRequest.class);

            // Validação de campos obrigatórios
            if (request.nomeCliente() == null || request.nomeCliente().trim().isEmpty()) {
                responderErro(ctx, HttpStatus.BAD_REQUEST, "Dados inválidos", "Campo 'nomeCliente' é obrigatório");
                return;
            }

            String status = request.status() != null ? request.status() : "ABERTO";

            Pedido pedido = pedidoService.atualizar(id, request.nomeCliente(), request.observacao(), status);

            Map<String, String> resposta = new HashMap<>();
            resposta.put("mensagem", "Pedido atualizado com sucesso");

            ctx.json(resposta);

        } catch (NaoEncontradoException e) {
            responderErro(ctx, HttpStatus.NOT_FOUND, "Pedido não encontrado", e.getMessage());
        } catch (ValidacaoException e) {
            responderErro(ctx, HttpStatus.BAD_REQUEST, "Dados inválidos", e.getMessage());
        } catch (IllegalArgumentException e) {
            responderErro(ctx, HttpStatus.BAD_REQUEST, "Dados inválidos", e.getMessage());
        } catch (Exception e) {
            System.err.println("ERRO AO ATUALIZAR: " + e.getMessage());
            e.printStackTrace();
            responderErro(ctx, HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", e.getMessage());
        }
    }

    /*
        Exclui um pedido pelo ID.
        DELETE /api/pedidos/{id}
    */
    public void excluir(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            pedidoService.excluir(id);
            ctx.status(HttpStatus.NO_CONTENT);

        } catch (NaoEncontradoException e) {
            responderErro(ctx, HttpStatus.NOT_FOUND, "Pedido não encontrado", e.getMessage());
        } catch (IllegalArgumentException e) {
            responderErro(ctx, HttpStatus.BAD_REQUEST, "ID inválido", e.getMessage());
        } catch (Exception e) {
            System.err.println("ERRO AO EXCLUIR: " + e.getMessage());
            e.printStackTrace();
            responderErro(ctx, HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", e.getMessage());
        }
    }

    // Métodos auxiliares

    /*
        Converte um Pedido em Map para serialização JSON.
    */
    private Map<String, Object> pedidoParaMap(Pedido pedido) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", pedido.getId().getValue());
        map.put("nomeCliente", pedido.getNomeCliente().getValue());
        map.put("observacao", pedido.getObservacao() != null ? pedido.getObservacao().getValue() : null);
        map.put("status", pedido.getStatus().name());
        map.put("produtoId", pedido.getProdutoId());
        map.put("criadoEm", pedido.getCriadoEm().format(FORMATO_DATA));
        map.put("atualizadoEm", pedido.getAtualizadoEm().format(FORMATO_DATA));
        return map;
    }

    /*
        Cria objeto de erro padronizado para respostas.
    */
    private Map<String, String> criarErro(String tipo, String mensagem) {
        Map<String, String> erro = new HashMap<>();
        erro.put("erro", tipo);
        erro.put("mensagem", mensagem);
        return erro;
    }

    /*
        Padroniza respostas de erro com status HTTP correto.
    */
    private void responderErro(Context ctx, HttpStatus status, String tipo, String mensagem) {
        ctx.status(status).json(criarErro(tipo, mensagem));
    }
}
