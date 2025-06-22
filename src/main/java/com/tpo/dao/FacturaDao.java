package com.tpo.dao;

import com.tpo.modelo.factura.Factura;
import java.util.List;
import java.util.Optional;

public interface FacturaDao {
    Factura guardar(Factura factura) throws Exception;
    void actualizarEstado(Long facturaId, String nuevoEstado) throws Exception;
    Optional<Factura> buscarPorId(Long facturaId) throws Exception;
    List<Factura> listarPorUsuario(Long usuarioId) throws Exception;
}
