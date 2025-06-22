package com.tpo.util;

import com.tpo.config.MongoFactory;
import com.tpo.config.RedisFactory;
import com.tpo.config.CassandraFactory;
import com.tpo.dao.mongo.ProductoDaoMongo;
import com.tpo.dao.mongo.RegistroCambioProductoDaoMongo;
import com.tpo.dao.redis.CarritoDaoRedis;
import com.tpo.dao.cassandra.SesionUsuarioDaoCassandra;
import com.tpo.dao.sql.UsuarioDaoSql;
import com.tpo.dao.sql.PedidoDaoSql;
import com.tpo.dao.sql.FacturaDaoSql;
import com.tpo.dao.sql.PagoDaoSql;
import com.tpo.modelo.usuario.Usuario;
import com.tpo.modelo.producto.Producto;
import com.tpo.modelo.pedido.LineaCarrito;
import com.tpo.modelo.pedido.Pedido;
import com.tpo.modelo.factura.Factura;
import com.tpo.modelo.pago.Pago;
import com.tpo.modelo.pago.MedioPago;
import com.tpo.modelo.usuario.RolUsuario;
import com.tpo.servicio.ServicioUsuario;
import com.tpo.servicio.ServicioProducto;
import com.tpo.servicio.ServicioCarrito;
import com.tpo.servicio.ServicioPedido;
import com.tpo.servicio.ServicioFactura;
import com.tpo.servicio.ServicioPago;
import com.tpo.servicio.ServicioReporte;
import com.tpo.dao.RegistroCambioProductoDao;
import com.tpo.dao.ProductoDao;
import com.tpo.dao.CarritoDao;
import com.tpo.dao.SesionUsuarioDao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class CargarDatos {

    private ServicioUsuario servicioUsuario;
    private ServicioProducto servicioProducto;
    private ServicioCarrito servicioCarrito;
    private ServicioPedido servicioPedido;
    private ServicioFactura servicioFactura;
    private ServicioPago servicioPago;
    private ServicioReporte servicioReporte;

    // DAOs directos por si se necesita omitir chequeos de rol
    private ProductoDao productoDaoMongo;
    private RegistroCambioProductoDao registroCambioProductoDaoMongo;

    public CargarDatos() throws Exception {
        iniciarServicios();
    }

    private void iniciarServicios() {
        // DAOs SQL
        UsuarioDaoSql usuarioDaoSql = new UsuarioDaoSql();
        PedidoDaoSql pedidoDaoSql = new PedidoDaoSql();
        FacturaDaoSql facturaDaoSql = new FacturaDaoSql();
        PagoDaoSql pagoDaoSql = new PagoDaoSql();

        // DAOs NoSQL
        productoDaoMongo = new ProductoDaoMongo();
        registroCambioProductoDaoMongo = new RegistroCambioProductoDaoMongo();
        CarritoDao carritoDaoRedis = new CarritoDaoRedis();
        SesionUsuarioDaoCassandra sesionUsuarioDaoCassandra = new SesionUsuarioDaoCassandra();

        // Servicios
        servicioUsuario = new ServicioUsuario(usuarioDaoSql, sesionUsuarioDaoCassandra);
        servicioProducto = new ServicioProducto(productoDaoMongo, registroCambioProductoDaoMongo);
        servicioCarrito = new ServicioCarrito(carritoDaoRedis);
        servicioPedido = new ServicioPedido(pedidoDaoSql, servicioCarrito, productoDaoMongo, servicioUsuario);
        servicioFactura = new ServicioFactura(facturaDaoSql, pedidoDaoSql, servicioUsuario, servicioPedido);
        servicioPago = new ServicioPago(pagoDaoSql, facturaDaoSql);
        servicioReporte = new ServicioReporte(sesionUsuarioDaoCassandra);
    }

    /**
     * Ejecutar poblamiento de datos de prueba.
     */
    public void poblarDatos() {
        try {
            System.out.println("=== Iniciando carga de datos de prueba ===");
            // 1. Crear usuarios (10)
            List<Usuario> usuarios = crearUsuariosPrueba(10);

            // 2. Designar primer usuario como ADMIN para creación de productos
            Usuario admin = usuarios.get(0);

            // 3. Crear productos (50)
            List<Producto> productos = crearProductosPrueba(50, admin);

            // 4. Crear pedidos: 2 por cada usuario (20 pedidos)
            crearPedidosPrueba(usuarios, productos);

            System.out.println("=== Carga de datos completada ===");
        } catch (Exception e) {
            System.err.println("Error en DataLoader: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crea N usuarios con datos dummy. Contraseña fija "1234567".
     * @param cantidad número de usuarios a crear
     * @return lista de Usuarios creados con ID asignado
     */
    private List<Usuario> crearUsuariosPrueba(int cantidad) throws Exception {
        List<Usuario> lista = new ArrayList<>();
        System.out.println("Creando " + cantidad + " usuarios de prueba...");
        for (int i = 1; i <= cantidad; i++) {
            Usuario u = new Usuario();
            u.setNombre("Cliente" + i);
            u.setApellido("Apellido" + i);
            u.setDireccion("Calle Falsa " + (100 + i));
            u.setDocIdentidad("DNI" + (10000000 + i));
            u.setEmail("cliente" + i + "@ejemplo.com");
            u.setContrasena("1234567");
            // Condición IVA: alternar o fijar todos a REGIMEN_GENERAL
            u.setCondicionIVA(com.tpo.modelo.usuario.CondicionIVA.REGIMEN_GENERAL);
            // Rol inicial: por defecto en ServicioUsuario será CLIENTE; asignaremos ADMIN luego al primero
            Usuario creado = servicioUsuario.registrarUsuario(u);
            System.out.println("  Usuario creado: ID=" + creado.getId()
                    + ", Nombre=" + creado.getNombre() + " " + creado.getApellido());
            lista.add(creado);
        }
        return lista;
    }




    private List<Producto> crearProductosPrueba(int cantidad, Usuario admin) {
        List<Producto> lista = new ArrayList<>();
        System.out.println("Creando " + cantidad + " productos de prueba...");
        Random rnd = new Random();
        for (int i = 1; i <= cantidad; i++) {
            try {
                Producto p = new Producto();
                p.setNombre("Producto" + i);
                p.setDescripcion("Descripción del producto " + i);
                // Precio aleatorio entre 10 y 200, con dos decimales
                BigDecimal precio = BigDecimal.valueOf(10 + rnd.nextDouble() * 190)
                        .setScale(2, BigDecimal.ROUND_HALF_UP);
                p.setPrecio(precio);
                // URLs fotos: una o dos de ejemplo
                List<String> fotos = new ArrayList<>();
                fotos.add("http://ejemplo.com/imagen" + i + ".jpg");
                if (rnd.nextBoolean()) {
                    fotos.add("http://ejemplo.com/imagen" + i + "_2.jpg");
                }
                p.setUrlsFotos(fotos);
                // URLs videos: ocasionalmente vacío o un URL
                if (rnd.nextInt(3) == 0) {
                    p.setUrlsVideos(Collections.singletonList("http://ejemplo.com/video" + i + ".mp4"));
                } else {
                    p.setUrlsVideos(new ArrayList<>());
                }
                // Comentarios iniciales vacíos
                p.setComentarios(new ArrayList<>());
                // Etiquetas: por ejemplo, “etiqueta{i%5}”
                List<String> etiquetas = new ArrayList<>();
                etiquetas.add("etiqueta" + (i % 5 + 1));
                if (rnd.nextBoolean()) {
                    etiquetas.add("categoria" + (i % 3 + 1));
                }
                p.setEtiquetas(etiquetas);
                // Fechas: servicio o DAO las asigna en persistencia. No seteamos fechaCreacion aquí, o se deja null.
                Producto creado = servicioProducto.crearProducto(p, String.valueOf(admin.getId()));
                System.out.println("  Producto creado: ID=" + creado.getId()
                        + ", Nombre=" + creado.getNombre() + ", Precio=" + creado.getPrecio());
                lista.add(creado);
            } catch (Exception e) {
                System.err.println("  Error al crear producto " + i + ": " + e.getMessage());
            }
        }
        return lista;
    }

    /**
     * Crea 2 pedidos por cada usuario en la lista: inicia carrito, añade elementos aleatorios, crea pedido,
     * factura y paga inmediatamente.
     */
    private void crearPedidosPrueba(List<Usuario> usuarios, List<Producto> productos) {
        System.out.println("Creando pedidos de prueba (2 por usuario)...");
        Random rnd = new Random();
        for (Usuario u : usuarios) {
            String userId = String.valueOf(u.getId());
            for (int p = 1; p <= 2; p++) {
                try {
                    // Iniciar carrito
                    servicioCarrito.iniciarCarrito(userId);
                    // Añadir entre 1 y 5 líneas
                    int lineas = 1 + rnd.nextInt(5);
                    Set<Integer> idxUsados = new HashSet<>();
                    for (int l = 0; l < lineas; l++) {
                        // Producto aleatorio no repetido en este carrito
                        int idx;
                        do {
                            idx = rnd.nextInt(productos.size());
                        } while (idxUsados.contains(idx));
                        idxUsados.add(idx);
                        Producto prod = productos.get(idx);
                        int cantidad = 1 + rnd.nextInt(3); // cantidad 1..3
                        servicioCarrito.agregarAlCarrito(userId, prod.getId(), cantidad, prod.getPrecio());
                    }
                    // Crear pedido
                    Pedido pedido = servicioPedido.crearPedidoDesdeCarrito(userId);
                    System.out.println("  Pedido creado para usuario ID=" + userId + ": PedidoID=" + pedido.getId());
                    // Facturar pedido
                    Factura factura = servicioFactura.facturarPedido(pedido.getId());
                    if (factura != null) {
                        System.out.println("    Factura creada: ID=" + factura.getId()
                                + ", Importe=" + factura.getImporteTotal());
                        // Registrar pago inmediato
                        List<Long> facturaIds = Collections.singletonList(factura.getId());
                        BigDecimal monto = factura.getImporteTotal();
                        MedioPago medio = MedioPago.EFECTIVO;
                        String operador = userId;
                        Pago pago = servicioPago.registrarPago(userId, facturaIds, monto, medio, operador);
                        System.out.println("    Pago registrado: ID=" + pago.getId()
                                + ", Monto=" + pago.getMontoTotal());
                    } else {
                        System.out.println("    No se generó factura para pedido ID=" + pedido.getId());
                    }
                } catch (Exception e) {
                    System.err.println("  Error al crear pedido para usuario ID=" + userId + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Método main para ejecutar el DataLoader independientemente.
     */
    public static void main(String[] args) {
        try {
            // Inicializar factorías si fuera necesario (opcional):
            // MongoFactory.getDatabase(); RedisFactory.getConnection(); CassandraFactory.getSession();
            CargarDatos loader = new CargarDatos();
            loader.poblarDatos();
        } catch (Exception e) {
            System.err.println("Error inicializando DataLoader: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
