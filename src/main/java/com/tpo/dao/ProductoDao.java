package com.tpo.dao;

import com.tpo.modelo.producto.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoDao {
    Producto guardar(Producto producto) throws Exception;
    void actualizar(Producto producto) throws Exception;
    void eliminarPorId(String id) throws Exception;
    Optional<Producto> buscarPorId(String id) throws Exception;
    List<Producto> buscarPorNombre(String nombre) throws Exception;
    List<Producto> listarTodos() throws Exception;
}
