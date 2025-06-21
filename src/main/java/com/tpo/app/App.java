package com.tpo.app;


import com.tpo.dao.sql.*;
import com.tpo.dao.mongo.*;
import com.tpo.dao.redis.*;
import com.tpo.dao.cassandra.*;
import com.tpo.modelo.pago.MedioPago;
import com.tpo.modelo.pago.Pago;
import com.tpo.modelo.usuario.RolUsuario;
import com.tpo.modelo.usuario.Usuario;
import com.tpo.servicio.*;
import com.tpo.modelo.producto.*;
import com.tpo.modelo.pedido.*;


import java.math.BigDecimal;
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
        PedidoDaoSql pedidoDaoSql = new PedidoDaoSql();
        FacturaDaoSql facturaDaoSql = new FacturaDaoSql();
        PagoDaoSql pagoDaoSql = new PagoDaoSql();
        // DAOs NoSQL
        ProductoDaoMongo productoDaoMongo = new ProductoDaoMongo();
        RegistroCambioProductoDaoMongo registroCambioProductoDaoMongo = new RegistroCambioProductoDaoMongo();
        CarritoDaoRedis carritoDaoRedis = new CarritoDaoRedis();
        SesionUsuarioDaoCassandra sesionUsuarioDaoCassandra = new SesionUsuarioDaoCassandra();

        // Servicios
        servicioUsuario = new ServicioUsuario(usuarioDaoSql, sesionUsuarioDaoCassandra);
        servicioProducto = new ServicioProducto(productoDaoMongo, registroCambioProductoDaoMongo);
        servicioCarrito = new ServicioCarrito(carritoDaoRedis);
        servicioPedido = new ServicioPedido(pedidoDaoSql, servicioCarrito, productoDaoMongo);
        servicioFactura = new ServicioFactura(facturaDaoSql, pedidoDaoSql);
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
                System.out.println("6) Deshacer carrito");
                System.out.println("7) Rehacer carrito");
                System.out.println("8) Crear pedido desde carrito");
                System.out.println("9) Facturar pedido");
                System.out.println("10) Registrar pago de factura");
                System.out.println("11) Logout");
                System.out.println("12) Ver categoría diaria");
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
                    case "6": opcionDeshacerCarrito(); break;
                    case "7": opcionRehacerCarrito(); break;
                    case "8": opcionCrearPedido(); break;
                    case "9": opcionFacturarPedido(); break;
                    case "10": opcionRegistrarPago(); break;
                    case "11": logout(); break;
                    case "12": opcionVerCategoria(); break;
                    case "0":
                        System.out.println("Saliendo...");
                        // Para forzar salida inmediata:
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
            Usuario u = new Usuario();
            u.setNombre(nombre);
            u.setApellido(apellido);
            u.setDireccion(direccion);
            u.setDocIdentidad(doc);
            u.setEmail(email);
            u.setContrasena(pass);
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
            System.out.print("Nombre producto: "); String nombre = scanner.nextLine();
            System.out.print("Descripción: "); String desc = scanner.nextLine();
            System.out.print("Precio unitario: "); BigDecimal precio = new BigDecimal(scanner.nextLine());
            Producto p = new Producto();
            p.setNombre(nombre);
            p.setDescripcion(desc);
            p.setPrecio(precio);
            // Inicializar listas vacías
            p.setUrlsFotos(new ArrayList<>());
            p.setUrlsVideos(new ArrayList<>());
            p.setComentarios(new ArrayList<>());
            p.setEtiquetas(new ArrayList<>());
            Producto creado = servicioProducto.crearProducto(p, String.valueOf(usuarioLogueado.getId()));
            System.out.println("Producto creado con ID: " + creado.getId());
        } catch (Exception e) {
            System.out.println("Error al crear producto: " + e.getMessage());
        }
    }

    private static void opcionListarProductos() {
        try {
            List<Producto> lista = servicioProducto.listarProductos();
            if (lista.isEmpty()) {
                System.out.println("No hay productos en el catálogo.");
            } else {
                System.out.println("Productos en catálogo:");
                for (Producto p : lista) {
                    System.out.println(p);
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
            System.out.print("ID Pedido a facturar: ");
            Long pedidoId = Long.parseLong(scanner.nextLine());
            var factura = servicioFactura.facturarPedido(pedidoId);
            System.out.println("Factura generada con ID: " + factura.getId());
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
            System.out.print("Fecha para categoría (YYYY-MM-DD): ");
            String fechaStr = scanner.nextLine();
            LocalDate fecha = LocalDate.parse(fechaStr);
            String categoria = servicioUsuario.calcularCategoriaDiaria(usuarioLogueado, fecha);
            System.out.println("Categoría para " + fecha + ": " + categoria);
        } catch (Exception e) {
            System.out.println("Error al obtener categoría: " + e.getMessage());
        }
    }
}
