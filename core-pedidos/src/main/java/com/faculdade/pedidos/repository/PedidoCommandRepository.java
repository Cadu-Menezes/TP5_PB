package com.faculdade.pedidos.repository;

import com.faculdade.pedidos.model.Pedido;
import com.faculdade.pedidos.model.PedidoId;

public interface PedidoCommandRepository {
    void criar(Pedido pedido);

    void atualizar(Pedido pedido);

    void excluir(PedidoId id);
}
