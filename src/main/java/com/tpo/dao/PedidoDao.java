package com.tpo.dao;

import com.tpo.modelo.pedido.Pedido;
import java.util.List;
import java.util.Optional;

public interface PedidoDao {
    Pedido guardar(Pedido pedido) throws Exception; // inserta Pedido y sus líneas en SQL
    void actualizarEstado(Long pedidoId, String nuevoEstado) throws Exception;
    Optional<Pedido> buscarPorId(Long pedidoId) throws Exception;
    List<Pedido> listarPorUsuario(Long usuarioId) throws Exception;
    List<Pedido> listarTodos() throws Exception;
}
