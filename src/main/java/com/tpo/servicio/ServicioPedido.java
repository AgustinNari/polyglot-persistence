package com.tpo.servicio;

import com.tpo.dao.PedidoDao;
import com.tpo.dao.ProductoDao;
import com.tpo.dao.CarritoDao;
import com.tpo.modelo.pedido.Pedido;
import com.tpo.modelo.pedido.LineaCarrito;
import com.tpo.modelo.pedido.LineaPedido;
import com.tpo.modelo.pedido.EstadoPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ServicioPedido {

    private final PedidoDao pedidoDao;
    private final ServicioCarrito servicioCarrito;
    private final ProductoDao productoDao;

    public ServicioPedido(PedidoDao pedidoDao, ServicioCarrito servicioCarrito, ProductoDao productoDao) {
        this.pedidoDao = pedidoDao;
        this.servicioCarrito = servicioCarrito;
        this.productoDao = productoDao;
    }

    /**
     * Crea un Pedido a partir del carrito del usuario.
     * Valida que cada producto exista en catálogo con precio actual.
     * Calcula descuentos/impuestos en Java (simplificado aquí).
     */
    public Pedido crearPedidoDesdeCarrito(String usuarioId) throws Exception {
        // Obtener líneas del carrito
        List<LineaCarrito> lineasCarrito = servicioCarrito.verCarrito(usuarioId);
        if (lineasCarrito.isEmpty()) {
            throw new IllegalStateException("El carrito está vacío");
        }
        Pedido pedido = new Pedido(Long.valueOf(usuarioId));
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.CREADO);
        // Para cada línea de carrito, validar producto en Mongo y obtener precio
        for (LineaCarrito lc : lineasCarrito) {
            String prodId = lc.getProductoId();
            // Buscar en catálogo
            var optProd = productoDao.buscarPorId(prodId);
            if (optProd.isEmpty()) {
                throw new IllegalArgumentException("Producto no existe en catálogo: " + prodId);
            }
            // Obtener precioUnitario actual desde catálogo
            BigDecimal precioActual = optProd.get().getPrecio();
            // Cantidad desde carrito
            int cantidad = lc.getCantidad();
            // Descuento e impuesto pueden depender de reglas (aquí simplificado: sin descuento, impuesto 0 o fijo)
            BigDecimal descuentoLinea = BigDecimal.ZERO;
            BigDecimal impuestoLinea = BigDecimal.ZERO;
            // Construir LineaPedido
            LineaPedido lp = new LineaPedido(prodId, cantidad, precioActual, descuentoLinea, impuestoLinea);
            lp.recalcularSubtotal(); // en LineaPedido implementado
            pedido.agregarLinea(lp);
        }
        // Guardar en SQL mediante PedidoDao
        Pedido guardado = pedidoDao.guardar(pedido);
        // Limpiar carrito
        servicioCarrito.limpiarCarrito(usuarioId);
        return guardado;
    }

    /**
     * Actualiza estado de pedido (opcional).
     */
    public void actualizarEstadoPedido(Long pedidoId, EstadoPedido nuevoEstado) throws Exception {
        pedidoDao.actualizarEstado(pedidoId, nuevoEstado.name());
    }
}
