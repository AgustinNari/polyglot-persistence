package com.tpo.app;


import com.tpo.dao.sql.*;
import com.tpo.dao.mongo.*;
import com.tpo.dao.redis.*;
import com.tpo.dao.cassandra.*;
import com.tpo.modelo.factura.Factura;
import com.tpo.modelo.pago.MedioPago;
import com.tpo.modelo.pago.Pago;
import com.tpo.modelo.sesion.SesionUsuario;
import com.tpo.modelo.usuario.CondicionIVA;
import com.tpo.modelo.usuario.RolUsuario;
import com.tpo.modelo.usuario.Usuario;
import com.tpo.servicio.*;
import com.tpo.modelo.producto.*;
import com.tpo.modelo.pedido.*;


import javax.swing.text.Document;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class App {

    private static final Scanner scanner = new Scanner(System.in);

    // Servicios
    private static ServicioUsuario servicioUsuario;
    private static ServicioProducto servicioProducto;
    private static ServicioCarrito servicioCarrito;
    private static ServicioPedido servicioPedido;
    private static ServicioFactura servicioFactura;
    private static ServicioPago servicioPago;
    private static ServicioReporte servicioReporte;
    private static PedidoDaoSql pedidoDaoSql = new PedidoDaoSql();
    private static FacturaDaoSql facturaDaoSql = new FacturaDaoSql();
    private static PagoDaoSql pagoDaoSql = new PagoDaoSql();
    private static SesionUsuarioDaoCassandra sesionUsuarioDaoCassandra = new SesionUsuarioDaoCassandra();
    private static RegistroCambioProductoDaoMongo registroCambioProductoDaoMongo = new RegistroCambioProductoDaoMongo();

    // Estado de sesión actual
    private static Usuario usuarioLogueado = null;
    private static LocalDateTime fechaLoginActual = null;

    public static void main(String[] args) {
        try {
            iniciarServicios();
            mostrarMenuPrincipal();
        } catch (Exception e) {
            System.err.println("Error en la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void iniciarServicios() {
        // DAOs SQL
        UsuarioDaoSql usuarioDaoSql = new UsuarioDaoSql();



        // DAOs NoSQL
        ProductoDaoMongo productoDaoMongo = new ProductoDaoMongo();
        CarritoDaoRedis carritoDaoRedis = new CarritoDaoRedis();


        // Servicios
        servicioUsuario = new ServicioUsuario(usuarioDaoSql, sesionUsuarioDaoCassandra);
        servicioProducto = new ServicioProducto(productoDaoMongo, registroCambioProductoDaoMongo);
        servicioCarrito = new ServicioCarrito(carritoDaoRedis);
        servicioPedido = new ServicioPedido(pedidoDaoSql, servicioCarrito, productoDaoMongo, servicioUsuario);
        servicioFactura = new ServicioFactura(facturaDaoSql, pedidoDaoSql, servicioUsuario, servicioPedido);
        servicioPago = new ServicioPago(pagoDaoSql, facturaDaoSql);
        servicioReporte = new ServicioReporte(sesionUsuarioDaoCassandra);
    }

    private static void mostrarMenuPrincipal() throws Exception {
        while (true) {
            System.out.println("\n--- Menú Principal ---");
            if (usuarioLogueado == null) {
                System.out.println("1) Registrar usuario");
                System.out.println("2) Login");
                System.out.println("0) Salir");
                System.out.print("Opción: ");
                String opcion = scanner.nextLine();
                switch (opcion) {
                    case "1": registrarUsuario(); break;
                    case "2": login(); break;
                    case "0": System.out.println("Saliendo...");
                        // Para forzar salida inmediata:
                        System.exit(0);
                        return;
                    default: System.out.println("Opción inválida"); break;
                }
            } else {
                System.out.println("Usuario logueado: " + usuarioLogueado.getNombre() + " " + usuarioLogueado.getApellido()
                        + " (Rol: " + usuarioLogueado.getRol() + ")");
                System.out.println("1) Agregar producto al catálogo (solo admin)");
                System.out.println("2) Listar productos");
                System.out.println("3) Iniciar carrito");
                System.out.println("4) Agregar al carrito");
                System.out.println("5) Ver carrito");
                System.out.println("6) Eliminar producto del carrito");
                System.out.println("7) Actualizar cantidad de producto en carrito");
                System.out.println("8) Deshacer carrito");
                System.out.println("9) Rehacer carrito");
                System.out.println("10) Crear pedido desde carrito");
                System.out.println("11) Facturar pedido");
                System.out.println("12) Registrar pago de factura");
                System.out.println("13) Ver Pedidos");
                System.out.println("14) Ver Facturas");
                System.out.println("15) Ver Pagos");
                System.out.println("16) Actualizar producto (solo admin)");
                System.out.println("17) Eliminar producto (solo admin)");
                System.out.println("18) Ver historial de cambios de producto (solo admin)");
                System.out.println("19) Ver historial de cambios de todos los productos (solo admin)");
                System.out.println("20) Ver reporte de sesión");
                System.out.println("21) Ver categoría");
                System.out.println("22) Cancelar pedido");
                System.out.println("23) Logout");
                System.out.println("0) Salir");
                System.out.print("Opción: ");
                String opcion = scanner.nextLine();
                switch (opcion) {
                    case "1":
                        if (usuarioLogueado.getRol() == RolUsuario.ADMIN) {
                            opcionAgregarProducto();
                        } else {
                            System.out.println("Acceso denegado: solo usuarios ADMIN pueden agregar productos.");
                        }
                        break;
                    case "2": opcionListarProductos(); break;
                    case "3": opcionIniciarCarrito(); break;
                    case "4": opcionAgregarAlCarrito(); break;
                    case "5": opcionVerCarrito(); break;
                    case "6": opcionEliminarDelCarrito(); break;
                    case "7": opcionActualizarCantidadCarrito(); break;
                    case "8": opcionDeshacerCarrito(); break;
                    case "9": opcionRehacerCarrito(); break;
                    case "10": opcionCrearPedido(); break;
                    case "11": opcionFacturarPedido(); break;
                    case "12": opcionRegistrarPago(); break;
                    case "13": opcionListarMisPedidos(); break;
                    case "14": opcionListarMisFacturas(); break;
                    case "15": opcionListarMisPagos(); break;
                    case "16":
                        if (usuarioLogueado.getRol() == RolUsuario.ADMIN) {
                            opcionActualizarProducto(); break;
                        } else {
                            System.out.println("Acceso denegado: solo usuarios ADMIN pueden actualizar productos.");
                        }
                        break;
                    case "17":
                        if (usuarioLogueado.getRol() == RolUsuario.ADMIN) {
                            opcionEliminarProducto(); break;
                        } else {
                            System.out.println("Acceso denegado: solo usuarios ADMIN pueden eliminar productos.");
                        }
                        break;
                    case "18":
                        if (usuarioLogueado.getRol() == RolUsuario.ADMIN) {
                            opcionVerHistorialProducto();
                        } else {
                            System.out.println("Acceso denegado: solo usuarios ADMIN pueden ver historial de cambios.");
                        }
                        break;
                    case "19":
                        if (usuarioLogueado.getRol() == RolUsuario.ADMIN) {
                            opcionVerHistorialCompleto();
                        } else {
                            System.out.println("Acceso denegado: solo usuarios ADMIN pueden ver historial completo.");
                        }
                        break;
                    case "20": opcionMostrarInfoSesion(); break;
                    case "21": opcionVerCategoria(); break;
                    case "22": opcionCancelarPedido(); break;
                    case "23": logout(); break;
                    case "0":
                        if (usuarioLogueado != null) {
                            // Informar al usuario
                            System.out.println("Saliendo de la aplicación: realizando logout automático...");
                            try {
                                // Llamar a logout con la fecha de login almacenada
                                servicioUsuario.logout(usuarioLogueado, fechaLoginActual);
                                System.out.println("Logout automático completado. Tiempo de sesión acumulado.");
                            } catch (Exception e) {
                                System.out.println("Error al realizar logout automático: " + e.getMessage());
                                // Opcional: loguear stacktrace en logger
                            }
                        } else {
                            System.out.println("Saliendo de la aplicación...");
                        }
                        // Finalmente, terminar
                        System.exit(0);
                        return;
                    default: System.out.println("Opción inválida"); break;
                }
            }
        }
    }

    private static void registrarUsuario() {
        try {
            System.out.print("Nombre: "); String nombre = scanner.nextLine();
            System.out.print("Apellido: "); String apellido = scanner.nextLine();
            System.out.print("Dirección: "); String direccion = scanner.nextLine();
            System.out.print("DocIdentidad: "); String doc = scanner.nextLine();
            System.out.print("Email: "); String email = scanner.nextLine();
            System.out.print("Contraseña: "); String pass = scanner.nextLine();
            System.out.println("Seleccione condición IVA:");
            System.out.println("(1) REGIMEN_GENERAL");
            System.out.println("(2) MONOTRIBUTISTA");
            System.out.println("(3) EXENTO");
            System.out.println("(4) EXPORTADOR");
            System.out.print("Condición IVA: ");
            String condIvaStr = scanner.nextLine();
            switch (condIvaStr) {
                case "1": condIvaStr = "REGIMEN_GENERAL"; break;
                case "2": condIvaStr = "MONOTRIBUTISTA"; break;
                case "3": condIvaStr = "EXENTO"; break;
                case "4": condIvaStr = "EXPORTADOR"; break;
                default: throw new IllegalArgumentException("Condición IVA inválida");
            }
            CondicionIVA condIva = CondicionIVA.valueOf(condIvaStr);
            if (condIva == null) {
                throw new IllegalArgumentException("Condición IVA inválida");
            }
            Usuario u = new Usuario();
            u.setNombre(nombre);
            u.setApellido(apellido);
            u.setDireccion(direccion);
            u.setDocIdentidad(doc);
            u.setEmail(email);
            u.setContrasena(pass);
            u.setCondicionIVA(condIva);
            // Rol se asigna en el servicio
            Usuario creado = servicioUsuario.registrarUsuario(u);
            System.out.println("Usuario registrado con ID: " + creado.getId() + ", Rol: " + creado.getRol());
        } catch (IllegalArgumentException iae) {
            System.out.println("Error en datos de usuario: " + iae.getMessage());
        } catch (Exception e) {
            System.out.println("Error al registrar usuario: " + e.getMessage());
        }
    }


    private static void login() {
        try {
            System.out.print("DocIdentidad: "); String doc = scanner.nextLine();
            System.out.print("Contraseña: "); String pass = scanner.nextLine();
            Usuario u = servicioUsuario.login(doc, pass);
            usuarioLogueado = u;
            fechaLoginActual = LocalDateTime.now();
            System.out.println("Login exitoso. Hora de login: " + fechaLoginActual);
        } catch (Exception e) {
            System.out.println("Error en login: " + e.getMessage());
        }
    }

    private static void logout() {
        try {
            if (usuarioLogueado != null && fechaLoginActual != null) {
                servicioUsuario.logout(usuarioLogueado, fechaLoginActual);
                System.out.println("Logout exitoso.");
                usuarioLogueado = null;
                fechaLoginActual = null;
            } else {
                System.out.println("No hay usuario logueado.");
            }
        } catch (Exception e) {
            System.out.println("Error en logout: " + e.getMessage());
        }
    }

    private static void opcionAgregarProducto() {
        try {
            System.out.println("=== Agregar nuevo producto ===");
            Producto p = new Producto();
            // Pedir datos básicos:
            System.out.print("Nombre: ");
            String nombre = scanner.nextLine().trim();
            p.setNombre(nombre);

            System.out.print("Descripción: ");
            String descripcion = scanner.nextLine().trim();
            p.setDescripcion(descripcion);

            System.out.print("Precio (ej. 100.50): ");
            String precioStr = scanner.nextLine().trim();
            try {
                p.setPrecio(new java.math.BigDecimal(precioStr));
            } catch (Exception e) {
                System.out.println("Precio inválido. Operación cancelada.");
                return;
            }

            // Fotos: pedir URLs separadas por comas o dejar vacío
            System.out.print("URLs de fotos (separadas por coma) o Enter para omitir: ");
            String fotosLine = scanner.nextLine().trim();
            if (!fotosLine.isBlank()) {
                String[] arrFotos = fotosLine.split(",");
                List<String> fotos = new java.util.ArrayList<>();
                for (String f : arrFotos) {
                    if (!f.isBlank()) fotos.add(f.trim());
                }
                p.setUrlsFotos(fotos);
            }

            // Videos
            System.out.print("URLs de videos (separadas por coma) o Enter para omitir: ");
            String videosLine = scanner.nextLine().trim();
            if (!videosLine.isBlank()) {
                String[] arrVideos = videosLine.split(",");
                List<String> videos = new java.util.ArrayList<>();
                for (String v : arrVideos) {
                    if (!v.isBlank()) videos.add(v.trim());
                }
                p.setUrlsVideos(videos);
            }

            // Comentarios iniciales opcional
            p.setComentarios(new java.util.ArrayList<>()); // vacío o podrías pedir

            // Etiquetas
            System.out.print("Etiquetas (separadas por coma) o Enter para omitir: ");
            String tagsLine = scanner.nextLine().trim();
            if (!tagsLine.isBlank()) {
                String[] arrTags = tagsLine.split(",");
                List<String> tags = new java.util.ArrayList<>();
                for (String t : arrTags) {
                    if (!t.isBlank()) tags.add(t.trim());
                }
                p.setEtiquetas(tags);
            }

            // Llamar servicio: operador = ID de usuario logueado
            String operador = String.valueOf(usuarioLogueado.getId());
            Producto creado = servicioProducto.crearProducto(p, operador);
            System.out.println("Producto creado con ID: " + creado.getId());
        } catch (Exception e) {
            System.out.println("Error al agregar producto: " + e.getMessage());
        }
    }


    private static void opcionListarProductos() {
        try {
            System.out.println("=== Listado de productos ===");
            List<Producto> lista = servicioProducto.listarProductos();
            if (lista.isEmpty()) {
                System.out.println("No hay productos en el catálogo.");
            } else {
                // Formato: ID, Nombre, Precio, FechaActualizacion
                System.out.printf("%-24s %-20s %-10s %-20s%n", "ID", "Nombre", "Precio", "Última actualización");
                for (Producto p : lista) {
                    String fechaAct = p.getFechaActualizacion() != null ? p.getFechaActualizacion().toString() : "-";
                    System.out.printf("%-24s %-20s %10.2f %-20s%n",
                            p.getId(),
                            p.getNombre(),
                            p.getPrecio() != null ? p.getPrecio() : java.math.BigDecimal.ZERO,
                            fechaAct);
                }
            }
        } catch (Exception e) {
            System.out.println("Error al listar productos: " + e.getMessage());
        }
    }



    private static void opcionIniciarCarrito() {
        try {
            servicioCarrito.iniciarCarrito(String.valueOf(usuarioLogueado.getId()));
            System.out.println("Carrito iniciado/limpio para usuario.");
        } catch (Exception e) {
            System.out.println("Error al iniciar carrito: " + e.getMessage());
        }
    }

    private static void opcionAgregarAlCarrito() {
        try {
            System.out.print("ID Producto a agregar: "); String prodId = scanner.nextLine();
            System.out.print("Cantidad: "); int cantidad = Integer.parseInt(scanner.nextLine());
            // Obtener precio actual desde catálogo
            var optP = servicioProducto.buscarProducto(prodId);
            if (optP.isEmpty()) {
                System.out.println("Producto no encontrado en catálogo.");
                return;
            }
            BigDecimal precio = optP.get().getPrecio();
            servicioCarrito.agregarAlCarrito(String.valueOf(usuarioLogueado.getId()), prodId, cantidad, precio);
            System.out.println("Línea agregada al carrito.");
        } catch (Exception e) {
            System.out.println("Error al agregar al carrito: " + e.getMessage());
        }
    }

    private static void opcionVerCarrito() {
        try {
            List<LineaCarrito> lineas = servicioCarrito.verCarrito(String.valueOf(usuarioLogueado.getId()));
            if (lineas.isEmpty()) {
                System.out.println("Carrito vacío.");
            } else {
                System.out.println("Contenido del carrito:");
                for (LineaCarrito l : lineas) {
                    System.out.println(l);
                }
            }
        } catch (Exception e) {
            System.out.println("Error al ver carrito: " + e.getMessage());
        }
    }

    private static void opcionEliminarDelCarrito() {
        try {
            System.out.print("Ingrese ID de producto a eliminar del carrito: ");
            String prodId = scanner.nextLine().trim();
            String userId = String.valueOf(usuarioLogueado.getId());
            servicioCarrito.eliminarLinea(userId, prodId);
            System.out.println("Producto eliminado del carrito.");
        } catch (Exception e) {
            System.out.println("Error al eliminar del carrito: " + e.getMessage());
        }
    }

    private static void opcionActualizarCantidadCarrito() {
        try {
            System.out.print("Ingrese ID de producto a actualizar cantidad: ");
            String prodId = scanner.nextLine().trim();
            System.out.print("Ingrese nueva cantidad: ");
            int nuevaCant = Integer.parseInt(scanner.nextLine().trim());
            String userId = String.valueOf(usuarioLogueado.getId());
            servicioCarrito.actualizarLinea(userId, prodId, nuevaCant,
                    servicioProducto.buscarProducto(prodId)
                            .map(Producto::getPrecio)
                            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado.")));
            System.out.println("Cantidad actualizada en carrito.");
        } catch (Exception e) {
            System.out.println("Error al actualizar cantidad en carrito: " + e.getMessage());
        }
    }


    private static void opcionDeshacerCarrito() {
        try {
            if (servicioCarrito.puedeDeshacer(String.valueOf(usuarioLogueado.getId()))) {
                servicioCarrito.deshacer(String.valueOf(usuarioLogueado.getId()));
                System.out.println("Deshacer aplicado.");
            } else {
                System.out.println("No hay acciones para deshacer.");
            }
        } catch (Exception e) {
            System.out.println("Error al deshacer: " + e.getMessage());
        }
    }

    private static void opcionRehacerCarrito() {
        try {
            if (servicioCarrito.puedeRehacer(String.valueOf(usuarioLogueado.getId()))) {
                servicioCarrito.rehacer(String.valueOf(usuarioLogueado.getId()));
                System.out.println("Rehacer aplicado.");
            } else {
                System.out.println("No hay acciones para rehacer.");
            }
        } catch (Exception e) {
            System.out.println("Error al rehacer: " + e.getMessage());
        }
    }

    private static void opcionCrearPedido() {
        try {
            Pedido pedido = servicioPedido.crearPedidoDesdeCarrito(String.valueOf(usuarioLogueado.getId()));
            System.out.println("Pedido creado con ID: " + pedido.getId());
        } catch (Exception e) {
            System.out.println("Error al crear pedido: " + e.getMessage());
        }
    }

    private static void opcionFacturarPedido() {
        try {
            System.out.print("Ingrese ID de pedido a facturar: ");
            Long pedidoId = Long.parseLong(scanner.nextLine());
            Factura factura = servicioFactura.facturarPedido(pedidoId);
            if (factura != null) {
                // ya imprimió detalle en el método crearFacturaDesdePedido
                System.out.println("Factura creada con ID: " + factura.getId());
            }
        } catch (Exception e) {
            System.out.println("Error al facturar pedido: " + e.getMessage());
        }
    }

    private static void opcionRegistrarPago() {
        try {
            System.out.print("IDs de facturas a pagar (separados por coma): ");
            String line = scanner.nextLine();
            String[] parts = line.split(",");
            List<Long> facturaIds = new ArrayList<>();
            for (String p : parts) {
                facturaIds.add(Long.parseLong(p.trim()));
            }
            System.out.print("Monto total del pago: ");
            BigDecimal monto = new BigDecimal(scanner.nextLine());
            System.out.println("Medio de pago (EFECTIVO, TARJETA, CTA_CTE, PUNTO_RETIRO, OTRO): ");
            MedioPago medio = MedioPago.valueOf(scanner.nextLine().trim().toUpperCase());
            System.out.print("Operador (o texto libre): ");
            String operador = scanner.nextLine();
            Pago pago = servicioPago.registrarPago(String.valueOf(usuarioLogueado.getId()), facturaIds, monto, medio, operador);
            System.out.println("Pago registrado con ID: " + pago.getId());
        } catch (Exception e) {
            System.out.println("Error al registrar pago: " + e.getMessage());
        }
    }

    private static void opcionVerCategoria() {
        try {
            String categoria = String.valueOf(servicioUsuario.obtenerCategoriaPromedio(usuarioLogueado));
            System.out.println("Categoría para " + usuarioLogueado.getNombre() + ": " + categoria);
        } catch (Exception e) {
            System.out.println("Error al obtener categoría: " + e.getMessage());
        }
    }

    private static void opcionListarMisPedidos() {
        try {
            Long usuarioId = usuarioLogueado.getId();
            List<Pedido> lista = servicioPedido.listarPedidosPorUsuario(String.valueOf(usuarioId));
            if (lista.isEmpty()) {
                System.out.println("No tiene pedidos.");
            } else {
                System.out.println("=== Mis Pedidos ===");
                // Encabezado alineado
                System.out.printf("%-5s %-20s %-12s %-12s%n", "ID", "FechaCreación", "Estado", "ImporteTotal");
                for (Pedido p : lista) {
                    String fecha = p.getFechaCreacion() != null
                            ? p.getFechaCreacion().toString()
                            : "N/D";
                    String estado = p.getEstado() != null ? p.getEstado().name() : "N/D";
                    BigDecimal importe = p.getImporteTotal() != null ? p.getImporteTotal() : BigDecimal.ZERO;
                    System.out.printf("%-5d %-20s %-12s %12.2f%n",
                            p.getId(), fecha, estado, importe);
                }
                // Si deseas, permitir ver detalle de un pedido específico:
                System.out.print("¿Ver detalle de algún pedido? Ingrese ID o Enter para omitir: ");
                String line = scanner.nextLine();
                if (!line.isBlank()) {
                    try {
                        Long id = Long.valueOf(line.trim());
                        Optional<Pedido> optP = ((PedidoDaoSql) pedidoDaoSql).buscarPorId(id);
                        if (optP.isPresent()) {
                            imprimirDetallePedido(optP.get());
                        } else {
                            System.out.println("Pedido no encontrado.");
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            System.out.println("Error al listar mis pedidos: " + e.getMessage());
        }
    }

    private static void imprimirDetallePedido(Pedido p) throws Exception {
        System.out.println("=== Detalle Pedido ID " + p.getId() + " ===");
        System.out.printf("FechaCreación: %s%n", p.getFechaCreacion());
        System.out.printf("Estado: %s%n", p.getEstado());
        System.out.println("--- Líneas ---");
        System.out.printf("%-10s %-20s %5s %12s %12s%n", "ProdID", "Nombre", "Cant", "PrecioU", "Subtotal");
        for (LineaPedido lp : p.getLineas()) {
            String prodId = lp.getProductoId();
            String nombre = servicioProducto.buscarProducto(prodId).map(Producto::getNombre).orElse("(desconocido)");
            BigDecimal subtotal = lp.getPrecioUnitario().multiply(BigDecimal.valueOf(lp.getCantidad()));
            System.out.printf("%-10s %-20s %5d %12.2f %12.2f%n",
                    prodId, nombre, lp.getCantidad(), lp.getPrecioUnitario(), subtotal);
        }
    }


    private static void opcionListarMisFacturas() {
        try {
            Long usuarioId = usuarioLogueado.getId();
            List<Factura> lista = servicioFactura.listarFacturasPorUsuario(usuarioId);
            if (lista.isEmpty()) {
                System.out.println("No tiene facturas.");
            } else {
                System.out.printf("%-5s %-20s %-10s %-12s%n", "ID", "FechaEmisión", "Estado", "ImporteTotal");
                for (Factura f : lista) {
                    System.out.printf("%-5d %-20s %-10s %12.2f%n",
                            f.getId(), f.getFechaEmision(), f.getEstado(), f.getImporteTotal());
                }
                System.out.print("¿Ver detalle de alguna factura? ID o Enter: ");
                String line = scanner.nextLine();
                if (!line.isBlank()) {
                    Long id = Long.valueOf(line.trim());
                    Optional<Factura> optF = facturaDaoSql.buscarPorId(id);
                    if (optF.isPresent()) {
                        servicioFactura.imprimirDetalleFactura(optF.get(), usuarioLogueado,
                                pedidoDaoSql.buscarPorId(optF.get().getPedidoId()).orElse(null));
                    } else {
                        System.out.println("Factura no encontrada.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error al listar mis facturas: " + e.getMessage());
        }
    }

    private static void opcionListarMisPagos() {
        try {
            Long usuarioId = usuarioLogueado.getId();
            List<Pago> lista = servicioPago.listarPagosPorUsuario(String.valueOf(usuarioId));
            if (lista.isEmpty()) {
                System.out.println("No tiene pagos registrados.");
            } else {
                System.out.printf("%-5s %-20s %-12s %-15s %-20s%n",
                        "ID", "FechaPago", "Monto", "MedioPago", "Operador");
                for (Pago p : lista) {
                    System.out.printf("%-5d %-20s %12.2f %-15s %-20s%n",
                            p.getId(), p.getFechaPago(), p.getMontoTotal(), p.getMedioPago(),
                            p.getOperador() != null ? p.getOperador() : "-");
                }
            }
        } catch (Exception e) {
            System.out.println("Error al listar mis pagos: " + e.getMessage());
        }
    }

    private static void opcionActualizarProducto() {
        try {
            System.out.print("Ingrese ID de producto a actualizar: ");
            String prodId = scanner.nextLine().trim();
            Optional<Producto> opt = servicioProducto.buscarProducto(prodId);
            if (opt.isEmpty()) {
                System.out.println("Producto no encontrado con ID: " + prodId);
                return;
            }
            Producto existente = opt.get();
            System.out.println("Valores actuales del producto:");
            System.out.println("ID: " + existente.getId());
            System.out.println("Nombre: " + existente.getNombre());
            System.out.println("Descripción: " + existente.getDescripcion());
            System.out.println("Precio: " + existente.getPrecio());
            System.out.println("URLs Fotos: " + existente.getUrlsFotos());
            System.out.println("URLs Videos: " + existente.getUrlsVideos());
            System.out.println("Etiquetas: " + existente.getEtiquetas());
            // Pedir nuevos valores; si la línea está en blanco, mantener valor actual.
            System.out.print("Nuevo nombre (Enter para dejar '" + existente.getNombre() + "'): ");
            String nombre = scanner.nextLine().trim();
            if (!nombre.isBlank()) {
                existente.setNombre(nombre);
            }
            System.out.print("Nueva descripción (Enter para dejar): ");
            String descripcion = scanner.nextLine().trim();
            if (!descripcion.isBlank()) {
                existente.setDescripcion(descripcion);
            }
            System.out.print("Nuevo precio (Enter para dejar " + existente.getPrecio() + "): ");
            String precioStr = scanner.nextLine().trim();
            if (!precioStr.isBlank()) {
                try {
                    existente.setPrecio(new java.math.BigDecimal(precioStr));
                } catch (Exception e) {
                    System.out.println("Precio inválido. Se mantiene el anterior.");
                }
            }
            // Fotos
            System.out.print("URLs Fotos (separadas por coma) o Enter para dejar actuales: ");
            String fotosLine = scanner.nextLine().trim();
            if (!fotosLine.isBlank()) {
                String[] arrFotos = fotosLine.split(",");
                List<String> fotos = new java.util.ArrayList<>();
                for (String f : arrFotos) {
                    if (!f.isBlank()) fotos.add(f.trim());
                }
                existente.setUrlsFotos(fotos);
            }
            // Videos
            System.out.print("URLs Videos (separadas por coma) o Enter para dejar actuales: ");
            String videosLine = scanner.nextLine().trim();
            if (!videosLine.isBlank()) {
                String[] arrVideos = videosLine.split(",");
                List<String> videos = new java.util.ArrayList<>();
                for (String v : arrVideos) {
                    if (!v.isBlank()) videos.add(v.trim());
                }
                existente.setUrlsVideos(videos);
            }
            // Etiquetas
            System.out.print("Etiquetas (separadas por coma) o Enter para dejar actuales: ");
            String tagsLine = scanner.nextLine().trim();
            if (!tagsLine.isBlank()) {
                String[] arrTags = tagsLine.split(",");
                List<String> tags = new java.util.ArrayList<>();
                for (String t : arrTags) {
                    if (!t.isBlank()) tags.add(t.trim());
                }
                existente.setEtiquetas(tags);
            }
            // Se asume comentarios no se actualizan manualmente aquí, o puede implementarse similar.
            // Llamar servicio
            String operador = String.valueOf(usuarioLogueado.getId());
            servicioProducto.actualizarProducto(existente, operador);
            System.out.println("Producto actualizado correctamente.");
        } catch (Exception e) {
            System.out.println("Error al actualizar producto: " + e.getMessage());
        }
    }


    private static void opcionEliminarProducto() {
        try {
            System.out.print("Ingrese ID de producto a eliminar: ");
            String prodId = scanner.nextLine().trim();
            System.out.print("¿Confirma eliminación de producto " + prodId + "? (S/N): ");
            String resp = scanner.nextLine().trim().toUpperCase();
            if (!resp.equals("S") && !resp.equals("SI")) {
                System.out.println("Operación cancelada.");
                return;
            }
            String operador = String.valueOf(usuarioLogueado.getId());
            servicioProducto.eliminarProducto(prodId, operador);
            System.out.println("Producto eliminado correctamente.");
        } catch (Exception e) {
            System.out.println("Error al eliminar producto: " + e.getMessage());
        }
    }


    private static void opcionVerHistorialProducto() {
        try {
            System.out.print("Ingrese el ID del producto para ver historial de cambios: ");
            String idProducto = scanner.nextLine().trim();
            if (idProducto.isEmpty()) {
                System.out.println("ID de producto vacío. Cancelado.");
                return;
            }
            List<RegistroCambioProducto> historial = servicioProducto.listarHistorialCambiosProducto(idProducto);
            if (historial.isEmpty()) {
                System.out.println("No se encontraron registros de cambios para el producto con ID: " + idProducto);
                return;
            }
            System.out.println("=== Historial de cambios para producto ID: " + idProducto + " ===");
            for (RegistroCambioProducto reg : historial) {
                System.out.println("------------------------------------------");
                System.out.println("Fecha cambio   : " + reg.getFechaCambio());
                System.out.println("Operador       : " + reg.getOperador());
                System.out.println("Tipo operación : " + reg.getTipoOperacion());
                System.out.println("Valor anterior :");
                System.out.print(ServicioProducto.formatearMapa(reg.getValorAnterior()));
                System.out.println("Valor nuevo    :");
                System.out.print(ServicioProducto.formatearMapa(reg.getValorNuevo()));
            }
            System.out.println("==========================================");
        } catch (IllegalArgumentException iae) {
            System.out.println("Error: " + iae.getMessage());
        } catch (Exception e) {
            System.out.println("Error al obtener historial de cambios: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            // e.printStackTrace(); // opcional en desarrollo
        }
    }

    private static void opcionVerHistorialCompleto() {
        try {
            List<RegistroCambioProducto> lista = registroCambioProductoDaoMongo.listarTodos();
            if (lista.isEmpty()) {
                System.out.println("No hay registros de cambios en catálogo.");
            } else {
                System.out.printf("%-24s %-10s %-20s %-10s%n", "FechaCambio", "Op", "ProductoID", "Operador");
                for (RegistroCambioProducto r : lista) {
                    String fecha = r.getFechaCambio() != null ? r.getFechaCambio().toString() : "-";
                    System.out.printf("%-24s %-10s %-20s %-10s%n",
                            fecha, r.getTipoOperacion(), r.getProductoId(), r.getOperador());
                }
                // Si se desea ver detalle de un registro en particular, habría que mostrar más info
            }
        } catch (Exception e) {
            System.out.println("Error al ver historial completo de catálogo: " + e.getMessage());
        }
    }


    private static void opcionMostrarInfoSesion() {
        try {
            System.out.println("=== Info usuario logueado ===");
            System.out.println("Nombre: " + usuarioLogueado.getNombre());
            System.out.println("Dirección: " + usuarioLogueado.getDireccion());
            System.out.println("DocIdentidad: " + usuarioLogueado.getDocIdentidad());

            // Obtener lista de sesiones
            List<SesionUsuario> sesiones = servicioUsuario.listarTodasSesiones(usuarioLogueado);
            if (sesiones.isEmpty()) {
                System.out.println("No hay registros de sesión para este usuario.");
            } else {
                System.out.printf("%-20s %-20s %-20s%n", "FechaLogin", "FechaLogout", "Duración(min)");
                for (SesionUsuario log : sesiones) {
                    LocalDateTime login = log.getFechaLogin();
                    LocalDateTime logout = log.getFechaLogout();
                    String logoutStr = (logout != null ? logout.toString() : "Sin logout");
                    long durMin = 0;
                    if (login != null) {
                        LocalDateTime fin = (logout != null ? logout : LocalDateTime.now());
                        durMin = java.time.Duration.between(login, fin).toMinutes();
                    }
                    System.out.printf("%-20s %-20s %10d%n",
                            (login!=null?login.toString():"-"),
                            logoutStr,
                            durMin);
                }
            }
        } catch (Exception e) {
            System.out.println("No se pudo recuperar historial de sesiones: " + e.getClass() + " " + e.getMessage());
        }
    }

    private static void opcionCancelarPedido() {
        try {
            if (usuarioLogueado == null) {
                System.out.println("Debe iniciar sesión para cancelar un pedido.");
                return;
            }
            System.out.print("Ingrese ID del pedido a cancelar: ");
            String idStr = scanner.nextLine().trim();
            if (idStr.isEmpty()) {
                System.out.println("ID vacío. Cancelado.");
                return;
            }
            Long pedidoId;
            try {
                pedidoId = Long.valueOf(idStr);
            } catch (NumberFormatException nfe) {
                System.out.println("ID de pedido inválido.");
                return;
            }
            // Opcional: verificar que el pedido pertenece al usuario logueado
            // Si tu DAO o servicio permite listarPorUsuario, verifica:
            List<Pedido> misPedidos = servicioPedido.listarPedidosPorUsuario(String.valueOf(usuarioLogueado.getId()));
            boolean esMio = misPedidos.stream().anyMatch(p -> p.getId().equals(pedidoId));
            if (!esMio) {
                System.out.println("No existe un pedido con ese ID para este usuario.");
                return;
            }
            // Intentar cancelar
            servicioPedido.cancelarPedido(pedidoId);
        } catch (IllegalArgumentException iae) {
            System.out.println("Error: " + iae.getMessage());
        } catch (IllegalStateException ise) {
            System.out.println("No se puede cancelar el pedido: " + ise.getMessage());
        } catch (Exception e) {
            System.out.println("Error al cancelar pedido: " + e.getMessage());
        }
    }





}
