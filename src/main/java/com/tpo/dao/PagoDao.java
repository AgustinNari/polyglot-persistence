package com.tpo.dao;

import com.tpo.modelo.pago.Pago;
import java.util.List;
import java.util.Optional;

public interface PagoDao {
    Pago guardar(Pago pago) throws Exception;
    Optional<Pago> buscarPorId(Long pagoId) throws Exception;
    List<Pago> listarPorUsuario(Long usuarioId) throws Exception;
    List<Pago> listarTodos() throws Exception;
    // Opcional: actualizar si se permite modificar pagos (normalmente no).
}
