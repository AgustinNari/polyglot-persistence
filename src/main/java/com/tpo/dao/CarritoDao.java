package com.tpo.dao;

import com.tpo.modelo.pedido.LineaCarrito;
import java.util.List;

public interface CarritoDao {
    void crearCarrito(String usuarioId) throws Exception;
    void eliminarCarrito(String usuarioId) throws Exception;
    void agregarLinea(String usuarioId, LineaCarrito linea) throws Exception;
    void actualizarLinea(String usuarioId, LineaCarrito linea) throws Exception;
    void eliminarLinea(String usuarioId, String productoId) throws Exception;
    List<LineaCarrito> obtenerLineas(String usuarioId) throws Exception;
    void limpiarCarrito(String usuarioId) throws Exception;
    // Métodos para historial de undo/redo:
    void registrarAccion(String usuarioId, String accion, LineaCarrito linea) throws Exception;
    boolean puedeDeshacer(String usuarioId) throws Exception;
    void deshacer(String usuarioId) throws Exception;
    boolean puedeRehacer(String usuarioId) throws Exception;
    void rehacer(String usuarioId) throws Exception;
}
