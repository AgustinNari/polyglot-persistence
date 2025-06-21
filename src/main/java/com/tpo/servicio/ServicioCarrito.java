package com.tpo.servicio;

import com.tpo.dao.CarritoDao;
import com.tpo.modelo.pedido.LineaCarrito;

import java.math.BigDecimal;
import java.util.List;

public class ServicioCarrito {

    private final CarritoDao carritoDao;

    public ServicioCarrito(CarritoDao carritoDao) {
        this.carritoDao = carritoDao;
    }

    public void iniciarCarrito(String usuarioId) throws Exception {
        carritoDao.crearCarrito(usuarioId);
    }

    public void agregarAlCarrito(String usuarioId, String productoId, int cantidad, BigDecimal precioUnitario) throws Exception {
        LineaCarrito linea = new LineaCarrito(productoId, cantidad, precioUnitario);
        linea.recalcularSubtotal();
        carritoDao.agregarLinea(usuarioId, linea);
        carritoDao.registrarAccion(usuarioId, "AGREGAR", linea);
    }

    public void actualizarLinea(String usuarioId, String productoId, int nuevaCantidad, BigDecimal nuevoPrecioUnitario) throws Exception {
        LineaCarrito linea = new LineaCarrito(productoId, nuevaCantidad, nuevoPrecioUnitario);
        linea.recalcularSubtotal();
        carritoDao.actualizarLinea(usuarioId, linea);
        carritoDao.registrarAccion(usuarioId, "ACTUALIZAR", linea);
    }

    public void eliminarLinea(String usuarioId, String productoId) throws Exception {
        // Antes de eliminar, podemos obtener la línea para registrar acción
        List<LineaCarrito> existentes = carritoDao.obtenerLineas(usuarioId);
        for (LineaCarrito l : existentes) {
            if (l.getProductoId().equals(productoId)) {
                carritoDao.registrarAccion(usuarioId, "ELIMINAR", l);
                break;
            }
        }
        carritoDao.eliminarLinea(usuarioId, productoId);
    }

    public List<LineaCarrito> verCarrito(String usuarioId) throws Exception {
        return carritoDao.obtenerLineas(usuarioId);
    }

    public void limpiarCarrito(String usuarioId) throws Exception {
        carritoDao.registrarAccion(usuarioId, "LIMPIAR", null);
        carritoDao.limpiarCarrito(usuarioId);
    }

    public boolean puedeDeshacer(String usuarioId) throws Exception {
        return carritoDao.puedeDeshacer(usuarioId);
    }

    public void deshacer(String usuarioId) throws Exception {
        carritoDao.deshacer(usuarioId);
    }

    public boolean puedeRehacer(String usuarioId) throws Exception {
        return carritoDao.puedeRehacer(usuarioId);
    }

    public void rehacer(String usuarioId) throws Exception {
        carritoDao.rehacer(usuarioId);
    }
}
