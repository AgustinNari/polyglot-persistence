package com.tpo.dao.sql;

import com.tpo.modelo.factura.Factura;
import com.tpo.modelo.pago.MedioPago;
import com.tpo.modelo.pago.Pago;
import com.tpo.modelo.pedido.LineaPedido;
import com.tpo.modelo.pedido.Pedido;
import com.tpo.modelo.usuario.RolUsuario;
import com.tpo.modelo.usuario.Usuario;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PagoDaoSqlTest {

    private UsuarioDaoSql usuarioDao;
    private PedidoDaoSql pedidoDao;
    private FacturaDaoSql facturaDao;
    private PagoDaoSql pagoDao;

    private Usuario usuarioTest;
    private Pedido pedidoTest;
    private Factura facturaTest;

    @BeforeAll
    void setup() throws Exception {
        usuarioDao = new UsuarioDaoSql();
        pedidoDao = new PedidoDaoSql();
        facturaDao = new FacturaDaoSql();
        pagoDao = new PagoDaoSql();
        // Crear usuario
        Usuario u = new Usuario("TestPago", "Usuario", "Dir Test Pago", "DNI-PAGO-TEST", "testpago@example.com", "passTest", RolUsuario.CLIENTE);
        usuarioTest = usuarioDao.guardar(u);
        // Crear pedido
        pedidoTest = new Pedido(usuarioTest.getId());
        LineaPedido linea = new LineaPedido("prod-pago-1", 1, new BigDecimal("150.00"), BigDecimal.ZERO, new BigDecimal("7.50"));
        linea.recalcularSubtotal();
        pedidoTest.agregarLinea(linea);
        pedidoTest = pedidoDao.guardar(pedidoTest);
        // Crear factura
        BigDecimal bruto = pedidoTest.getImporteTotal();
        BigDecimal descuento = BigDecimal.ZERO;
        BigDecimal impuesto = new BigDecimal("7.50"); // si tu subtotalFinal incluye impuesto, asegúrate de lógica
        facturaTest = new Factura(pedidoTest.getId(), usuarioTest.getId(), bruto, descuento, impuesto);
        facturaTest = facturaDao.guardar(facturaTest);
    }

    @AfterAll
    void teardown() throws Exception {
        // Eliminar registros en orden inverso: Pago, Factura, Pedido, Usuario
        // Primero eliminar pagos asociados (si quedan)
        // Luego facturas
        // Luego pedido, luego usuario
        // Para simplicidad, eliminar facturaTest y pedidoTest y usuarioTest:
        try (var conn = com.tpo.config.SqlServerFactory.getConnection()) {
            // Eliminar pagos asociados si existen
            try (var ps = conn.prepareStatement("DELETE FROM dbo.Factura_Pago WHERE factura_id = ?")) {
                ps.setLong(1, facturaTest.getId());
                ps.executeUpdate();
            }
            try (var ps = conn.prepareStatement("DELETE FROM dbo.Pagos WHERE usuario_id = ?")) {
                ps.setLong(1, usuarioTest.getId());
                ps.executeUpdate();
            }
            // Eliminar factura
            try (var ps = conn.prepareStatement("DELETE FROM dbo.Facturas WHERE id = ?")) {
                ps.setLong(1, facturaTest.getId());
                ps.executeUpdate();
            }
            // Eliminar pedido
            try (var ps1 = conn.prepareStatement("DELETE FROM dbo.LineaPedido WHERE pedido_id = ?");
                 var ps2 = conn.prepareStatement("DELETE FROM dbo.Pedidos WHERE id = ?")) {
                ps1.setLong(1, pedidoTest.getId());
                ps1.executeUpdate();
                ps2.setLong(1, pedidoTest.getId());
                ps2.executeUpdate();
            }
        }
        // Eliminar usuario
        if (usuarioTest != null && usuarioTest.getId() != null) {
            usuarioDao.eliminarPorId(usuarioTest.getId());
        }
    }

    @Test
    void testGuardarYBuscarPago() throws Exception {
        // Supongamos que cliente paga la única facturaTest
        Pago pago = new Pago(usuarioTest.getId(), facturaTest.getImporteTotal(), MedioPago.EFECTIVO, "Caja1", Arrays.asList(facturaTest.getId()));
        Pago guardado = pagoDao.guardar(pago);
        assertNotNull(guardado.getId());

        Optional<Pago> recOpt = pagoDao.buscarPorId(guardado.getId());
        assertTrue(recOpt.isPresent());
        Pago rec = recOpt.get();
        assertEquals(usuarioTest.getId(), rec.getUsuarioId());
        assertEquals(0, rec.getMontoTotal().compareTo(pago.getMontoTotal()));
        assertEquals(1, rec.getFacturaIds().size());
        assertEquals(facturaTest.getId(), rec.getFacturaIds().get(0));

        // Limpiar: eliminar pago y su relación
        try (var conn = com.tpo.config.SqlServerFactory.getConnection()) {
            try (var ps = conn.prepareStatement("DELETE FROM dbo.Factura_Pago WHERE pago_id = ?")) {
                ps.setLong(1, guardado.getId());
                ps.executeUpdate();
            }
            try (var ps = conn.prepareStatement("DELETE FROM dbo.Pagos WHERE id = ?")) {
                ps.setLong(1, guardado.getId());
                ps.executeUpdate();
            }
        }
    }
}
