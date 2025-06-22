package com.tpo.util;


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

import com.tpo.modelo.pedido.Pedido;
import com.tpo.modelo.factura.Factura;
import com.tpo.modelo.pago.Pago;
import com.tpo.modelo.pago.MedioPago;

import com.tpo.servicio.ServicioUsuario;
import com.tpo.servicio.ServicioProducto;
import com.tpo.servicio.ServicioCarrito;
import com.tpo.servicio.ServicioPedido;
import com.tpo.servicio.ServicioFactura;
import com.tpo.servicio.ServicioPago;

import com.tpo.dao.RegistroCambioProductoDao;
import com.tpo.dao.ProductoDao;
import com.tpo.dao.CarritoDao;


import java.math.BigDecimal;

import java.util.*;

public class CargarDatos {

    private ServicioUsuario servicioUsuario;
    private ServicioProducto servicioProducto;
    private ServicioCarrito servicioCarrito;
    private ServicioPedido servicioPedido;
    private ServicioFactura servicioFactura;
    private ServicioPago servicioPago;


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
    }


    public void poblarDatos() {
        try {
            System.out.println("=== Iniciando carga de datos de prueba ===");
            List<Usuario> usuarios = crearUsuariosPrueba(10);

            Usuario admin = usuarios.get(0);


            List<Producto> productos = crearProductosPrueba(50, admin);


            crearPedidosPrueba(usuarios, productos);

            System.out.println("=== Carga de datos completada ===");
        } catch (Exception e) {
            System.err.println("Error en DataLoader: " + e.getMessage());
            e.printStackTrace();
        }
    }


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

            u.setCondicionIVA(com.tpo.modelo.usuario.CondicionIVA.REGIMEN_GENERAL);

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

                BigDecimal precio = BigDecimal.valueOf(10 + rnd.nextDouble() * 190)
                        .setScale(2, BigDecimal.ROUND_HALF_UP);
                p.setPrecio(precio);

                List<String> fotos = new ArrayList<>();
                fotos.add("http://ejemplo.com/imagen" + i + ".jpg");
                if (rnd.nextBoolean()) {
                    fotos.add("http://ejemplo.com/imagen" + i + "_2.jpg");
                }
                p.setUrlsFotos(fotos);

                if (rnd.nextInt(3) == 0) {
                    p.setUrlsVideos(Collections.singletonList("http://ejemplo.com/video" + i + ".mp4"));
                } else {
                    p.setUrlsVideos(new ArrayList<>());
                }

                p.setComentarios(new ArrayList<>());

                List<String> etiquetas = new ArrayList<>();
                etiquetas.add("etiqueta" + (i % 5 + 1));
                if (rnd.nextBoolean()) {
                    etiquetas.add("categoria" + (i % 3 + 1));
                }
                p.setEtiquetas(etiquetas);

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


    private void crearPedidosPrueba(List<Usuario> usuarios, List<Producto> productos) {
        System.out.println("Creando pedidos de prueba (2 por usuario)...");
        Random rnd = new Random();
        for (Usuario u : usuarios) {
            String userId = String.valueOf(u.getId());
            for (int p = 1; p <= 2; p++) {
                try {

                    servicioCarrito.iniciarCarrito(userId);

                    int lineas = 1 + rnd.nextInt(5);
                    Set<Integer> idxUsados = new HashSet<>();
                    for (int l = 0; l < lineas; l++) {

                        int idx;
                        do {
                            idx = rnd.nextInt(productos.size());
                        } while (idxUsados.contains(idx));
                        idxUsados.add(idx);
                        Producto prod = productos.get(idx);
                        int cantidad = 1 + rnd.nextInt(3);
                        servicioCarrito.agregarAlCarrito(userId, prod.getId(), cantidad, prod.getPrecio());
                    }

                    Pedido pedido = servicioPedido.crearPedidoDesdeCarrito(userId);
                    System.out.println("  Pedido creado para usuario ID=" + userId + ": PedidoID=" + pedido.getId());

                    Factura factura = servicioFactura.facturarPedido(pedido.getId());
                    if (factura != null) {
                        System.out.println("    Factura creada: ID=" + factura.getId()
                                + ", Importe=" + factura.getImporteTotal());

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


    public static void main(String[] args) {
        try {

            CargarDatos loader = new CargarDatos();
            loader.poblarDatos();
        } catch (Exception e) {
            System.err.println("Error inicializando DataLoader: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
