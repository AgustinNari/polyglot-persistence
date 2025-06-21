package com.tpo.dao;

import com.tpo.modelo.producto.RegistroCambioProducto;
import java.util.List;

public interface RegistroCambioProductoDao {
    RegistroCambioProducto guardar(RegistroCambioProducto registro) throws Exception;
    List<RegistroCambioProducto> listarPorProducto(String productoId) throws Exception;
    List<RegistroCambioProducto> listarTodos() throws Exception;
}
