package com.tpo.servicio;

import com.tpo.dao.FacturaDao;
import com.tpo.dao.PedidoDao;
import com.tpo.modelo.pedido.Pedido;
import com.tpo.modelo.factura.Factura;
import com.tpo.modelo.factura.EstadoFactura;
import com.tpo.modelo.pedido.EstadoPedido;
import com.tpo.modelo.usuario.Usuario;
import com.tpo.servicio.ServicioUsuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    /**
     * Genera una factura a partir de un pedido existente. Calcula importes: bruto, descuentos, impuestos.
     * Luego guarda en SQL y actualiza estado de pedido a FACTURADO.
     */
    public Factura facturarPedido(Long pedidoId) throws Exception {
        // 1. Obtener pedido
        Pedido pedido = pedidoDao.buscarPorId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID " + pedidoId));
        // 2. Verificar estado del pedido (por ejemplo, solo si está CREADO)
        if (!pedido.getEstado().equals(EstadoPedido.CREADO)) {
            throw new IllegalStateException("El pedido no está en estado CREADO, no se puede facturar");
        }
        // 3. Obtener usuario y datos fiscales
        Usuario usuario = servicioUsuario.buscarUsuarioPorId(pedido.getUsuarioId());
        // 4. Construir objeto Factura con datos de pedido
        Factura factura = new Factura();
        factura.setPedidoId(pedidoId);
        factura.setUsuarioId(pedido.getUsuarioId());
        factura.setFechaEmision(LocalDateTime.now());
        factura.setEstado(EstadoFactura.PENDIENTE_PAGO); // por ejemplo enum con PENDIENTE, PAGADA, etc.
        // 5. Calcular importes (usamos los cálculos ya almacenados en Pedido)
        BigDecimal bruto = pedido.getImporteBruto();
        BigDecimal desc = pedido.getDescuentoTotal();
        BigDecimal iva = pedido.getImpuestoTotal();
        BigDecimal total = pedido.getImporteTotal();
        factura.setImporteBruto(bruto);
        factura.setDescuentoTotal(desc);
        factura.setImpuestoTotal(iva);
        factura.setImporteTotal(total);
        // 6. Persistir factura en SQL
        Factura guardada = facturaDao.guardar(factura);
        // 7. Mostrar detalle completo por consola
        imprimirDetalleFactura(guardada, usuario, pedido);
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

    public List<Factura> listarFacturasPorUsuario(Long usuarioId) throws Exception {
        return facturaDao.listarPorUsuario(usuarioId);
    }
    /**
     * Cambia el estado de una factura (por ejemplo a PAGADA o CANCELADA).
     */
    public void actualizarEstadoFactura(Long facturaId, EstadoFactura nuevoEstado) throws Exception {
        facturaDao.actualizarEstado(facturaId, nuevoEstado.name());
    }
}
