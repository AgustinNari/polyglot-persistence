package com.tpo.util;

import com.tpo.config.SqlServerFactory;
import com.tpo.config.MongoFactory;
import com.tpo.config.CassandraFactory;
import com.tpo.config.RedisFactory;

import com.tpo.modelo.sesion.SesionUsuario;
import com.tpo.servicio.*;
import com.tpo.dao.sql.UsuarioDaoSql;
import com.tpo.dao.cassandra.SesionUsuarioDaoCassandra;
import com.tpo.dao.mongo.ProductoDaoMongo;
import com.tpo.dao.mongo.RegistroCambioProductoDaoMongo;
import com.tpo.dao.redis.CarritoDaoRedis;
import com.tpo.dao.sql.PedidoDaoSql;
import com.tpo.dao.sql.FacturaDaoSql;
import com.tpo.dao.sql.PagoDaoSql;

import com.tpo.modelo.usuario.Usuario;
import com.tpo.modelo.producto.Producto;
import com.tpo.modelo.pago.MedioPago;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.*;

public class DataLoader {

    private static ServicioUsuario servicioUsuario;
    private static ServicioProducto servicioProducto;
    private static ServicioCarrito servicioCarrito;
    private static ServicioPedido servicioPedido;
    private static ServicioFactura servicioFactura;
    private static ServicioPago servicioPago;
    private static SesionUsuarioDaoCassandra sesionUsuarioDao;

    public static void main(String[] args) {
        try {
            // 1. Vaciar datos en todas las BD
            vaciarSQL();
            vaciarMongo();
            vaciarRedis();
            vaciarCassandra();

            // 2. Inicializar servicios
            iniciarServicios();

            // 3. Crear usuarios de prueba
            List<Usuario> usuarios = crearUsuariosPrueba(10);

            // 4. Crear productos de prueba
            List<Producto> productos = crearProductosPrueba(50);

            // 5. Simular sesiones en Cassandra
            simularSesiones(usuarios);

            // 6. Para algunos usuarios, simular carritos y pedidos
            simularPedidosYFacturas(usuarios, productos);

            System.out.println("DataLoader completado.");
        } catch (Exception e) {
            System.err.println("Error en DataLoader: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    private static void iniciarServicios() {
        UsuarioDaoSql usuarioDaoSql = new UsuarioDaoSql();
        sesionUsuarioDao = new SesionUsuarioDaoCassandra();
        servicioUsuario = new ServicioUsuario(usuarioDaoSql, sesionUsuarioDao);

        ProductoDaoMongo productoDaoMongo = new ProductoDaoMongo();
        RegistroCambioProductoDaoMongo registroCambioProductoDaoMongo = new RegistroCambioProductoDaoMongo();
        servicioProducto = new ServicioProducto(productoDaoMongo, registroCambioProductoDaoMongo);

        CarritoDaoRedis carritoDaoRedis = new CarritoDaoRedis();
        servicioCarrito = new ServicioCarrito(carritoDaoRedis);

        PedidoDaoSql pedidoDaoSql = new PedidoDaoSql();
        servicioPedido = new ServicioPedido(pedidoDaoSql, servicioCarrito, productoDaoMongo);

        FacturaDaoSql facturaDaoSql = new FacturaDaoSql();
        servicioFactura = new ServicioFactura(facturaDaoSql, pedidoDaoSql);

        PagoDaoSql pagoDaoSql = new PagoDaoSql();
        servicioPago = new ServicioPago(pagoDaoSql, facturaDaoSql);
    }

    /** Vaciar SQL: tablas dependientes primero */
    public static void vaciarSQL() throws Exception {
        try (Connection conn = SqlServerFactory.getConnection()) {
            conn.setAutoCommit(false);
            try (var st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM dbo.LineaPedido");
                st.executeUpdate("DELETE FROM dbo.Factura_Pago");
                st.executeUpdate("DELETE FROM dbo.Pagos");
                st.executeUpdate("DELETE FROM dbo.Facturas");
                st.executeUpdate("DELETE FROM dbo.Pedidos");
                st.executeUpdate("DELETE FROM dbo.Users");
                st.executeUpdate("DBCC CHECKIDENT('dbo.Users', RESEED, 0)"); // Reiniciar ID
                conn.commit();
                System.out.println("SQL: Tablas vaciadas");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Vaciar Mongo: colecciones products y product_history */
    public static void vaciarMongo() {
        var client = MongoFactory.getClient(); // si tu factory expone getClient()
        var db = client.getDatabase(MongoFactory.getDatabaseName());
        db.getCollection("products").deleteMany(new org.bson.Document());
        db.getCollection("product_history").deleteMany(new org.bson.Document());
        System.out.println("Mongo: colecciones vaciadas");
    }

    /** Vaciar Redis */
    public static void vaciarRedis() {
        try (var jedis = RedisFactory.getConnection()) {
            jedis.flushDB();
            System.out.println("Redis: vaciado");
        }
    }

    /** Vaciar Cassandra */
    public static void vaciarCassandra() {
        var session = CassandraFactory.getSession();
        session.execute("TRUNCATE tpo.user_logs");
        System.out.println("Cassandra: user_logs truncada");
    }

    /** Crear N usuarios de prueba */
    private static List<Usuario> crearUsuariosPrueba(int n) throws Exception {
        List<Usuario> lista = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            String nombre = "User" + i;
            String apellido = "Apellido" + i;
            String direccion = "Calle Falsa " + i;
            String doc = "DNI-TEST-" + i;
            String email = "user" + i + "@example.com";
            String pass = "password" + (100 + i); // >=6 caracteres
            Usuario u = new Usuario();
            u.setNombre(nombre);
            u.setApellido(apellido);
            u.setDireccion(direccion);
            u.setDocIdentidad(doc);
            u.setEmail(email);
            u.setContrasena(pass);
            // rol se asigna en registrarUsuario: el primero será ADMIN, luego CLIENTE
            Usuario creado = servicioUsuario.registrarUsuario(u);
            System.out.println("Usuario creado: " + creado);
            lista.add(creado);
        }
        return lista;
    }

    /** Crear M productos de prueba */
    private static List<Producto> crearProductosPrueba(int m) throws Exception {
        List<Producto> lista = new ArrayList<>();
        for (int i = 1; i <= m; i++) {
            Producto p = new Producto();
            p.setNombre("Producto" + i);
            p.setDescripcion("Descripción del producto " + i);
            BigDecimal precio = BigDecimal.valueOf(10 + i);
            p.setPrecio(precio);
            p.setUrlsFotos(List.of("http://example.com/foto" + i + ".jpg"));
            p.setUrlsVideos(List.of());
            p.setComentarios(List.of());
            p.setEtiquetas(List.of("etiqueta" + (i%5)));
            Producto creado = servicioProducto.crearProducto(p, "seed-loader");
            System.out.println("Producto creado: " + creado.getId());
            lista.add(creado);
        }
        return lista;
    }

    /** Simular sesiones en Cassandra para cada usuario, con login/logout en diferentes días/hours */
    private static void simularSesiones(List<Usuario> usuarios) throws Exception {
        for (Usuario u : usuarios) {
            // Simular 2 sesiones hoy para cada usuario
            for (int j = 0; j < 2; j++) {
                LocalDateTime loginTime = LocalDateTime.now().minusHours(new Random().nextInt(5)).withNano(0);
                // Registrar inicio
                SesionUsuario log = new SesionUsuario();
                log.setUsuarioId(String.valueOf(u.getId()));
                log.setFechaLogin(loginTime);
                sesionUsuarioDao.registrarInicioSesion(log);
                // Registrar logout 30 minutos después
                LocalDateTime logoutTime = loginTime.plusMinutes(30 + new Random().nextInt(30));
                sesionUsuarioDao.registrarLogout(String.valueOf(u.getId()), loginTime.toLocalDate(), loginTime, logoutTime);
            }
        }
        System.out.println("Sesiones simuladas en Cassandra");
    }

    /** Simular carritos, pedidos, facturas y pagos */
    private static void simularPedidosYFacturas(List<Usuario> usuarios, List<Producto> productos) throws Exception {
        Random rnd = new Random();
        int contadorPedidos = 0;
        for (Usuario u : usuarios) {
            // Para algunos usuarios (por ejemplo, la mitad), crear 2 pedidos cada uno
            if (rnd.nextBoolean()) {
                for (int k = 0; k < 2; k++) {
                    String userIdStr = String.valueOf(u.getId());
                    // Iniciar carrito
                    servicioCarrito.iniciarCarrito(userIdStr);
                    // Agregar de 1 a 3 productos aleatorios
                    int lineas = 1 + rnd.nextInt(3);
                    for (int li = 0; li < lineas; li++) {
                        Producto p = productos.get(rnd.nextInt(productos.size()));
                        int cantidad = 1 + rnd.nextInt(5);
                        BigDecimal precioActual = p.getPrecio();
                        servicioCarrito.agregarAlCarrito(userIdStr, p.getId(), cantidad, precioActual);
                    }
                    // Crear pedido
                    var pedido = servicioPedido.crearPedidoDesdeCarrito(userIdStr);
                    System.out.println("Pedido creado: " + pedido.getId() + " para usuario " + u.getId());
                    contadorPedidos++;
                    // Facturar pedido
                    var factura = servicioFactura.facturarPedido(pedido.getId());
                    System.out.println("Factura creada: " + factura.getId());
                    // Registrar pago
                    List<Long> listaFacturas = List.of(factura.getId());
                    BigDecimal monto = factura.getImporteTotal();
                    // Elegir medio aleatorio
                    MedioPago medio = MedioPago.values()[rnd.nextInt(MedioPago.values().length)];
                    String operador = medio == MedioPago.TARJETA ? "OperadorTarjeta" : null;
                    servicioPago.registrarPago(userIdStr, listaFacturas, monto, medio, operador);
                    System.out.println("Pago registrado para factura " + factura.getId());
                }
            }
        }
        System.out.println("Se generaron " + contadorPedidos + " pedidos+facturas+pagos de prueba.");
    }
}
