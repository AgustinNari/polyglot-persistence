package com.tpo.servicio;

import com.tpo.dao.FacturaDao;
import com.tpo.dao.PedidoDao;
import com.tpo.modelo.pedido.Pedido;
import com.tpo.modelo.factura.Factura;
import com.tpo.modelo.factura.EstadoFactura;
import com.tpo.modelo.pedido.EstadoPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class ServicioFactura {

    private final FacturaDao facturaDao;
    private final PedidoDao pedidoDao;

    public ServicioFactura(FacturaDao facturaDao, PedidoDao pedidoDao) {
        this.facturaDao = facturaDao;
        this.pedidoDao = pedidoDao;
    }

    /**
     * Genera una factura a partir de un pedido existente. Calcula importes: bruto, descuentos, impuestos.
     * Luego guarda en SQL y actualiza estado de pedido a FACTURADO.
     */
    public Factura facturarPedido(Long pedidoId) throws Exception {
        Optional<Pedido> optPedido = pedidoDao.buscarPorId(pedidoId);
        if (optPedido.isEmpty()) {
            throw new IllegalArgumentException("Pedido no encontrado: " + pedidoId);
        }
        Pedido pedido = optPedido.get();
        // Calcular importe bruto: suma de subtotales de lineas
        BigDecimal importeBruto = pedido.getTotal(); // si getTotal suma precios*cantidades
        // Descuentos e impuestos totales: simplificado a cero
        BigDecimal descuentoTotal = BigDecimal.ZERO;
        BigDecimal impuestoTotal = BigDecimal.ZERO;
        // Crear Factura
        Factura factura = new Factura(pedidoId, pedido.getUsuarioId(), importeBruto, descuentoTotal, impuestoTotal);
        factura.setFechaEmision(LocalDateTime.now());
        factura.setEstado(EstadoFactura.PENDIENTE_PAGO);
        Factura guardada = facturaDao.guardar(factura);
        // Actualizar estado del pedido a FACTURADO
        pedidoDao.actualizarEstado(pedidoId, EstadoPedido.FACTURADO.name());
        return guardada;
    }

    /**
     * Cambia el estado de una factura (por ejemplo a PAGADA o CANCELADA).
     */
    public void actualizarEstadoFactura(Long facturaId, EstadoFactura nuevoEstado) throws Exception {
        facturaDao.actualizarEstado(facturaId, nuevoEstado.name());
    }
}
