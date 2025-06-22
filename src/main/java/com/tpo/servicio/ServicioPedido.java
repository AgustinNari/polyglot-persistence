package com.tpo.servicio;

import com.tpo.dao.PedidoDao;
import com.tpo.dao.ProductoDao;
import com.tpo.modelo.pedido.LineaCarrito;
import com.tpo.modelo.pedido.LineaPedido;
import com.tpo.modelo.pedido.Pedido;
import com.tpo.modelo.pedido.EstadoPedido;
import com.tpo.modelo.producto.Producto;
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


    public Pedido crearPedidoDesdeCarrito(String usuarioIdStr) throws Exception {

        Long usuarioId = Long.valueOf(usuarioIdStr);
        Optional<Usuario> optUsuario = Optional.ofNullable(servicioUsuario.buscarUsuarioPorId(usuarioId));
        if (optUsuario.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado con ID " + usuarioIdStr);
        }
        Usuario usuario = optUsuario.get();

        CategoriaUsuario categoria = servicioUsuario.obtenerCategoriaPromedio(usuario);

        var condicionIVA = usuario.getCondicionIVA();


        List<LineaCarrito> lineasCarrito = servicioCarrito.verCarrito(usuarioIdStr);
        if (lineasCarrito.isEmpty()) {
            System.out.println("El carrito está vacío. No se puede crear pedido.");
            return null;
        }


        Pedido pedido = new Pedido(usuarioId);
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.CREADO);


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
            BigDecimal alicuota = BigDecimal.valueOf(0.21);
            int cantidad = lc.getCantidad();


            BigDecimal subtotalLinea = precioActual.multiply(BigDecimal.valueOf(cantidad));


            BigDecimal porcentajeDesc = DescuentoUtil.porcentajeDescuento(categoria);
            BigDecimal descuentoLinea = DescuentoUtil.calcularDescuento(subtotalLinea, porcentajeDesc);


            BigDecimal baseConDescuento = subtotalLinea.subtract(descuentoLinea);


            BigDecimal impuestoLinea = ImpuestoUtil.calcularIVA(baseConDescuento, condicionIVA, alicuota);


            BigDecimal totalLinea = baseConDescuento.add(impuestoLinea);


            LineaPedido lp = new LineaPedido(prodId, cantidad, precioActual, descuentoLinea, impuestoLinea);

            pedido.agregarLinea(lp);


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


        System.out.println("=== Resumen ===");
        System.out.printf("Importe bruto (suma subtotales): %.2f%n", pedido.getImporteBruto());
        System.out.printf("Descuento total (%s): %.2f%n", categoria, pedido.getDescuentoTotal());
        System.out.printf("IVA total (%s): %.2f%n", condicionIVA, pedido.getImpuestoTotal());
        System.out.printf("Importe total a pagar: %.2f%n", pedido.getImporteTotal());


        Scanner sc = new Scanner(System.in);
        System.out.print("¿Desea confirmar el pedido? (S/N): ");
        String resp = sc.nextLine().trim().toUpperCase();
        if (!resp.equals("S") && !resp.equals("SI")) {
            System.out.println("Pedido cancelado.");
            return null;
        }


        Pedido guardado = pedidoDao.guardar(pedido);



        servicioCarrito.limpiarCarrito(usuarioIdStr);
        System.out.println("Carrito limpiado tras creación de pedido.");

        return guardado;
    }


    public void actualizarEstadoPedido(Long pedidoId, EstadoPedido nuevoEstado) throws Exception {
        pedidoDao.actualizarEstado(pedidoId, nuevoEstado.name());
    }


    public List<Pedido> listarPedidosPorUsuario(String usuarioIdStr) throws Exception {
        Long usuarioId = Long.valueOf(usuarioIdStr);
        if (pedidoDao instanceof com.tpo.dao.sql.PedidoDaoSql) {

            return ((com.tpo.dao.sql.PedidoDaoSql) pedidoDao).listarPorUsuario(usuarioId);
        } else {

            throw new UnsupportedOperationException("listarPorUsuario no implementado en este DAO");
        }
    }

    public void cancelarPedido(Long pedidoId) throws Exception {

        Optional<Pedido> optPedido = pedidoDao.buscarPorId(pedidoId);
        if (optPedido.isEmpty()) {
            throw new IllegalArgumentException("Pedido no encontrado con ID " + pedidoId);
        }
        Pedido pedido = optPedido.get();

        if (!pedido.getEstado().equals(EstadoPedido.CREADO)) {
            throw new IllegalStateException("Solo se puede cancelar un pedido en estado CREADO. Estado actual: " + pedido.getEstado());
        }

        pedidoDao.actualizarEstado(pedidoId, EstadoPedido.CANCELADO.name());
        System.out.println("Pedido con ID " + pedidoId + " ha sido CANCELADO.");
    }
}
