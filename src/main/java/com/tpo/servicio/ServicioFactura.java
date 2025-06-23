package com.tpo.servicio;

import com.tpo.dao.FacturaDao;
import com.tpo.dao.PedidoDao;
import com.tpo.modelo.pedido.Pedido;
import com.tpo.modelo.factura.Factura;
import com.tpo.modelo.factura.EstadoFactura;
import com.tpo.modelo.pedido.EstadoPedido;
import com.tpo.modelo.usuario.Usuario;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


public class ServicioFactura {

    private final FacturaDao facturaDao;
    private final PedidoDao pedidoDao;
    private final ServicioUsuario servicioUsuario;
    private final ServicioPedido servicioPedido;

    public ServicioFactura(FacturaDao facturaDao, PedidoDao pedidoDao,
                           ServicioUsuario servicioUsuario, ServicioPedido servicioPedido)  {
        this.facturaDao = facturaDao;
        this.pedidoDao = pedidoDao;
        this.servicioUsuario = servicioUsuario;
        this.servicioPedido = servicioPedido;
    }


    public Factura facturarPedido(Long pedidoId) throws Exception {

        Pedido pedido = pedidoDao.buscarPorId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID " + pedidoId));

        if (!pedido.getEstado().equals(EstadoPedido.CREADO)) {
            throw new IllegalStateException("El pedido no está en estado CREADO, no se puede facturar");
        }

        Usuario usuario = servicioUsuario.buscarUsuarioPorId(pedido.getUsuarioId());

        Factura factura = new Factura();
        factura.setPedidoId(pedidoId);
        factura.setUsuarioId(pedido.getUsuarioId());
        factura.setFechaEmision(LocalDateTime.now());
        factura.setEstado(EstadoFactura.PENDIENTE_PAGO);

        BigDecimal bruto = pedido.getImporteBruto();
        BigDecimal desc = pedido.getDescuentoTotal();
        BigDecimal iva = pedido.getImpuestoTotal();
        BigDecimal total = pedido.getImporteTotal();
        factura.setImporteBruto(bruto);
        factura.setDescuentoTotal(desc);
        factura.setImpuestoTotal(iva);
        factura.setImporteTotal(total);

        Factura guardada = facturaDao.guardar(factura);

        servicioPedido.actualizarEstadoPedido(pedidoId, EstadoPedido.FACTURADO);

        imprimirDetalleFactura(guardada, usuario, pedido);
        return guardada;
    }


    public Factura facturarPedidoMono(Long pedidoId) throws Exception {

        Pedido pedido = pedidoDao.buscarPorId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID " + pedidoId));

        if (!pedido.getEstado().equals(EstadoPedido.CREADO)) {
            throw new IllegalStateException("El pedido no está en estado CREADO, no se puede facturar");
        }

        Usuario usuario = servicioUsuario.buscarUsuarioPorId(pedido.getUsuarioId());

        Factura factura = new Factura();
        factura.setPedidoId(pedidoId);
        factura.setUsuarioId(pedido.getUsuarioId());
        factura.setFechaEmision(LocalDateTime.now());
        factura.setEstado(EstadoFactura.PENDIENTE_PAGO);

        BigDecimal bruto = pedido.getImporteBruto();
        BigDecimal desc = pedido.getDescuentoTotal();
        BigDecimal iva = pedido.getImpuestoTotal();
        BigDecimal total = pedido.getImporteTotal();
        factura.setImporteBruto(bruto);
        factura.setDescuentoTotal(desc);
        factura.setImpuestoTotal(iva);
        factura.setImporteTotal(total);

        Factura guardada = facturaDao.guardar(factura);

        servicioPedido.actualizarEstadoPedido(pedidoId, EstadoPedido.FACTURADO);

        imprimirDetalleFacturaMono(guardada, usuario, pedido);
        return guardada;
    }


    public void imprimirDetalleFactura(Factura factura, Usuario usuario, Pedido pedido) throws Exception {
        System.out.println("\n=== Detalle de Factura ===");
        System.out.printf("ID Factura: %d%n", factura.getId());
        System.out.printf("Fecha Emisión: %s%n", factura.getFechaEmision());
        System.out.printf("Estado: %s%n", factura.getEstado());
        System.out.printf("Pedido asociado: %d%n", factura.getPedidoId());
        System.out.println("--- Datos del Cliente ---");
        System.out.printf("ID Usuario: %d%n", usuario.getId());
        System.out.printf("Nombre: %s %s%n", usuario.getNombre(), usuario.getApellido());
        System.out.printf("DocIdentidad: %s%n", usuario.getDocIdentidad());
        System.out.printf("Dirección: %s%n", usuario.getDireccion());
        System.out.printf("Condición IVA: %s%n", usuario.getCondicionIVA());
        System.out.println("--- Importes ---");
        System.out.printf("Importe bruto: %.2f%n", factura.getImporteBruto());
        System.out.printf("Descuento total: %.2f%n", factura.getDescuentoTotal());
        System.out.printf("IVA total: %.2f%n", factura.getImpuestoTotal());
        System.out.printf("Importe total a pagar: %.2f%n", factura.getImporteTotal());
        System.out.println("===========================\n");
    }

    public void imprimirDetalleFacturaMono(Factura factura, Usuario usuario, Pedido pedido) throws Exception {
        System.out.println("\n=== Detalle de Factura ===");
        System.out.printf("ID Factura: %d%n", factura.getId());
        System.out.printf("Fecha Emisión: %s%n", factura.getFechaEmision());
        System.out.printf("Estado: %s%n", factura.getEstado());
        System.out.printf("Pedido asociado: %d%n", factura.getPedidoId());
        System.out.println("--- Datos del Cliente ---");
        System.out.printf("ID Usuario: %d%n", usuario.getId());
        System.out.printf("Nombre: %s %s%n", usuario.getNombre(), usuario.getApellido());
        System.out.printf("DocIdentidad: %s%n", usuario.getDocIdentidad());
        System.out.printf("Dirección: %s%n", usuario.getDireccion());
        System.out.printf("Condición IVA: %s%n", usuario.getCondicionIVA());
        System.out.println("--- Importes ---");
        System.out.printf("Importe bruto: %.2f%n", factura.getImporteBruto());
        System.out.printf("Descuento total: %.2f%n", factura.getDescuentoTotal());
        System.out.printf("Importe total a pagar: %.2f%n", factura.getImporteTotal());
        System.out.println("===========================\n");
    }

    public List<Factura> listarFacturasPorUsuario(Long usuarioId) throws Exception {
        return facturaDao.listarPorUsuario(usuarioId);
    }

}
