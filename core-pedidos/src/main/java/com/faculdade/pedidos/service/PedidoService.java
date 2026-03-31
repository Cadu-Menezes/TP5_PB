package com.faculdade.pedidos.service;

import com.faculdade.pedidos.exception.NaoEncontradoException;
import com.faculdade.pedidos.model.*;
import com.faculdade.pedidos.repository.PedidoCommandRepository;
import com.faculdade.pedidos.repository.PedidoQueryRepository;

import java.util.List;

/*
    Serviço de aplicação para regras do CRUD.
    Mantém comandos e queries separados (CQS) via repositórios.
*/
public final class PedidoService {

    private final PedidoCommandRepository commandRepository;
    private final PedidoQueryRepository queryRepository;

    public PedidoService(PedidoCommandRepository commandRepository, PedidoQueryRepository queryRepository) {
        this.commandRepository = commandRepository;
        this.queryRepository = queryRepository;
    }

    public Pedido criar(String nomeCliente, String observacao, String status) {
        return criar(nomeCliente, observacao, status, null);
    }

    public Pedido criar(String nomeCliente, String observacao, String status, String produtoId) {
        Pedido pedido = Pedido.criar(
                NomeCliente.of(nomeCliente),
                Observacao.ofNullable(observacao),
                StatusPedido.fromString(status),
                produtoId
        );
        commandRepository.criar(pedido);
        return pedido;
    }

    public List<Pedido> listarTodos() {
        return queryRepository.listarTodos();
    }

    public Pedido atualizar(String id, String nomeCliente, String observacao, String status) {
        PedidoId pedidoId = PedidoId.of(id);
        Pedido existente = queryRepository.buscarPorId(pedidoId)
                .orElseThrow(() -> new NaoEncontradoException("Pedido não encontrado."));

        Pedido atualizado = existente.atualizar(
                NomeCliente.of(nomeCliente),
                Observacao.ofNullable(observacao),
                StatusPedido.fromString(status)
        );

        commandRepository.atualizar(atualizado);
        return atualizado;
    }

    public void excluir(String id) {
        PedidoId pedidoId = PedidoId.of(id);
        if (queryRepository.buscarPorId(pedidoId).isEmpty()) {
            throw new NaoEncontradoException("Pedido não encontrado.");
        }
        commandRepository.excluir(pedidoId);
    }
}
