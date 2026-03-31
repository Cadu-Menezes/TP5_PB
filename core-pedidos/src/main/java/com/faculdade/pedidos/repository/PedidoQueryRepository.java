package com.faculdade.pedidos.repository;

import com.faculdade.pedidos.model.Pedido;
import com.faculdade.pedidos.model.PedidoId;

import java.util.List;
import java.util.Optional;

public interface PedidoQueryRepository {
    Optional<Pedido> buscarPorId(PedidoId id);

    List<Pedido> listarTodos();
}
