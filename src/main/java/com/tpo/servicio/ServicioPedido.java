package com.tpo.servicio;

import com.tpo.dao.PedidoDao;
import com.tpo.dao.ProductoDao;
import com.tpo.modelo.pedido.LineaCarrito;
import com.tpo.modelo.pedido.LineaPedido;
import com.tpo.modelo.pedido.Pedido;
import com.tpo.modelo.pedido.EstadoPedido;
import com.tpo.modelo.producto.Producto; // asumiendo paquete
import com.tpo.modelo.usuario.Usuario;
import com.tpo.modelo.usuario.CategoriaUsuario;
import com.tpo.util.DescuentoUtil;
import com.tpo.util.ImpuestoUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ServicioPedido {

    private final PedidoDao pedidoDao;
    private final ServicioCarrito servicioCarrito;
    private final ProductoDao productoDao;
    private final ServicioUsuario servicioUsuario;

    public ServicioPedido(PedidoDao pedidoDao,
                          ServicioCarrito servicioCarrito,
                          ProductoDao productoDao,
                          ServicioUsuario servicioUsuario) {
        this.pedidoDao = pedidoDao;
        this.servicioCarrito = servicioCarrito;
        this.productoDao = productoDao;
        this.servicioUsuario = servicioUsuario;
    }

    /**
     * Crea un Pedido a partir del carrito del usuario, calculando descuentos e IVA según categoría y condición.
     * Muestra un resumen detallado y pide confirmación antes de persistir.
     * @param usuarioIdStr ID de usuario en String.
     * @return Pedido creado o null si se canceló.
     * @throws Exception
     */
    public Pedido crearPedidoDesdeCarrito(String usuarioIdStr) throws Exception {
        // 1. Obtener usuario y datos fiscales/categoría
        Long usuarioId = Long.valueOf(usuarioIdStr);
        Optional<Usuario> optUsuario = Optional.ofNullable(servicioUsuario.buscarUsuarioPorId(usuarioId));
        if (optUsuario.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado con ID " + usuarioIdStr);
        }
        Usuario usuario = optUsuario.get();
        // Obtener categoría promedio
        CategoriaUsuario categoria = servicioUsuario.obtenerCategoriaPromedio(usuario);
        // Obtener condición IVA
        var condicionIVA = usuario.getCondicionIVA(); // tipo CondicionIVA

        // 2. Obtener líneas del carrito
        List<LineaCarrito> lineasCarrito = servicioCarrito.verCarrito(usuarioIdStr);
        if (lineasCarrito.isEmpty()) {
            System.out.println("El carrito está vacío. No se puede crear pedido.");
            return null;
        }

        // 3. Preparar objeto Pedido
        Pedido pedido = new Pedido(usuarioId);
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.CREADO);

        // 4. Calcular detalle línea a línea
        System.out.println("=== Detalle preliminar del pedido ===");
        System.out.printf("%-22s   %-20s  %5s  %10s  %10s    %10s  %10s     %12s%n",
                "ProdID", "Nombre", "Cant", "PrecioU", "Subt", "Desc", "IVA", "TotalLinea");
        for (LineaCarrito lc : lineasCarrito) {
            String prodId = lc.getProductoId();
            Optional<Producto> optProd = productoDao.buscarPorId(prodId);
            if (optProd.isEmpty()) {
                System.out.println("  [Omisión] Producto no existe en catálogo: " + prodId);
                continue;
            }
            Producto p = optProd.get();
            BigDecimal precioActual = p.getPrecio();
            BigDecimal alicuota = BigDecimal.valueOf(0.21); // Ej. 0.21
            int cantidad = lc.getCantidad();

            // 4.1 Subtotal línea
            BigDecimal subtotalLinea = precioActual.multiply(BigDecimal.valueOf(cantidad));

            // 4.2 Descuento línea según categoría
            BigDecimal porcentajeDesc = DescuentoUtil.porcentajeDescuento(categoria);
            BigDecimal descuentoLinea = DescuentoUtil.calcularDescuento(subtotalLinea, porcentajeDesc);

            // 4.3 Base con descuento
            BigDecimal baseConDescuento = subtotalLinea.subtract(descuentoLinea);

            // 4.4 IVA línea según condición IVA
            BigDecimal impuestoLinea = ImpuestoUtil.calcularIVA(baseConDescuento, condicionIVA, alicuota);

            // 4.5 Total línea (opcional mostrar)
            BigDecimal totalLinea = baseConDescuento.add(impuestoLinea);

            // 4.6 Construir objeto LineaPedido
            LineaPedido lp = new LineaPedido(prodId, cantidad, precioActual, descuentoLinea, impuestoLinea);
            // recalcularSubtotal dentro del constructor:
            // lp.recalcularSubtotal(); // ya se hace en constructor
            pedido.agregarLinea(lp); // actualiza totales en el pedido

            // 4.7 Imprimir línea
            System.out.printf("%-10s %-20s %5d %12.2f %12.2f %12.2f %12.2f %12.2f%n",
                    prodId,
                    p.getNombre(),
                    cantidad,
                    precioActual,
                    subtotalLinea,
                    descuentoLinea,
                    impuestoLinea,
                    totalLinea);
        }

        // 5. Mostrar resumen general
        System.out.println("=== Resumen ===");
        System.out.printf("Importe bruto (suma subtotales): %.2f%n", pedido.getImporteBruto());
        System.out.printf("Descuento total (%s): %.2f%n", categoria, pedido.getDescuentoTotal());
        System.out.printf("IVA total (%s): %.2f%n", condicionIVA, pedido.getImpuestoTotal());
        System.out.printf("Importe total a pagar: %.2f%n", pedido.getImporteTotal());

        // 6. Confirmación
        Scanner sc = new Scanner(System.in);
        System.out.print("¿Desea confirmar el pedido? (S/N): ");
        String resp = sc.nextLine().trim().toUpperCase();
        if (!resp.equals("S") && !resp.equals("SI")) {
            System.out.println("Pedido cancelado.");
            return null;
        }

        // 7. Persistir el pedido
        Pedido guardado = pedidoDao.guardar(pedido);
        System.out.println("Pedido creado con ID: " + guardado.getId());

        // 8. Limpiar carrito
        servicioCarrito.limpiarCarrito(usuarioIdStr);
        System.out.println("Carrito limpiado tras creación de pedido.");

        return guardado;
    }

    /**
     * Actualiza estado de pedido.
     */
    public void actualizarEstadoPedido(Long pedidoId, EstadoPedido nuevoEstado) throws Exception {
        pedidoDao.actualizarEstado(pedidoId, nuevoEstado.name());
    }

    /**
     * Lista todos los pedidos de un usuario.
     */
    public List<Pedido> listarPedidosPorUsuario(String usuarioIdStr) throws Exception {
        Long usuarioId = Long.valueOf(usuarioIdStr);
        if (pedidoDao instanceof com.tpo.dao.sql.PedidoDaoSql) {
            // uso método específico
            return ((com.tpo.dao.sql.PedidoDaoSql) pedidoDao).listarPorUsuario(usuarioId);
        } else {
            // Si tu interfaz PedidoDao define listarPorUsuario, úsalo:
            // return pedidoDao.listarPorUsuario(usuarioId);
            throw new UnsupportedOperationException("listarPorUsuario no implementado en este DAO");
        }
    }
}
