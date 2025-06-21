package com.tpo.dao.sql;

import com.tpo.dao.FacturaDao;
import com.tpo.dao.PedidoDao;
import com.tpo.dao.UsuarioDao;
import com.tpo.modelo.factura.Factura;
import com.tpo.modelo.pedido.LineaPedido;
import com.tpo.modelo.pedido.Pedido;
import com.tpo.modelo.usuario.RolUsuario;
import com.tpo.modelo.usuario.Usuario;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FacturaDaoSqlTest {

    private UsuarioDaoSql usuarioDao;
    private PedidoDaoSql pedidoDao;
    private FacturaDaoSql facturaDao;
    private Usuario usuarioTest;
    private Pedido pedidoTest;

    @BeforeAll
    void setup() throws Exception {
        usuarioDao = new UsuarioDaoSql();
        pedidoDao = new PedidoDaoSql();
        facturaDao = new FacturaDaoSql();

        // Generar valores únicos para docIdentidad y email usando timestamp
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        // Asegurarse que la longitud total < 100; con timestamp actual, suele ser < 30 chars
        String docIdentidadTest = "DNI-FACT-TEST-" + uniqueSuffix;
        String emailTest = "testfact-" + uniqueSuffix + "@example.com";

        // Crear usuario con datos únicos; con column length ampliado a NVARCHAR(100) esto no truncará
        Usuario u = new Usuario("TestFactura", "Usuario", "Dir Test", docIdentidadTest, emailTest, "passTest", RolUsuario.CLIENTE);
        usuarioTest = usuarioDao.guardar(u);

        // Crear pedido asociado al usuario recién creado
        pedidoTest = null;
        try {
            pedidoTest = new Pedido(usuarioTest.getId());
            LineaPedido linea = new LineaPedido("prod-fact-1", 1, new BigDecimal("200.00"), BigDecimal.ZERO, new BigDecimal("10.00"));
            linea.recalcularSubtotal(); // importante recalcular antes de agregar
            pedidoTest.agregarLinea(linea);
            pedidoTest = pedidoDao.guardar(pedidoTest);
        } catch (Exception e) {
            // Si falla la creación de pedido, aseguramos que pedidoTest quede nulo y evitamos NPE en teardown
            pedidoTest = null;
            throw e;
        }
    }

    @AfterAll
    void teardown() throws Exception {
        // Limpiar registros en orden inverso: Facturas (eliminadas en test), Pedido, Usuario
        // Solo si pedidoTest no es nulo
        if (pedidoTest != null && pedidoTest.getId() != null) {
            try (var conn = com.tpo.config.SqlServerFactory.getConnection()) {
                // Eliminar líneas y pedido
                try (var psLineas = conn.prepareStatement("DELETE FROM dbo.LineaPedido WHERE pedido_id = ?");
                     var psPedidos = conn.prepareStatement("DELETE FROM dbo.Pedidos WHERE id = ?")) {
                    psLineas.setLong(1, pedidoTest.getId());
                    psLineas.executeUpdate();
                    psPedidos.setLong(1, pedidoTest.getId());
                    psPedidos.executeUpdate();
                }
            }
        }
        // Finalmente, eliminar usuarioTest
        if (usuarioTest != null && usuarioTest.getId() != null) {
            usuarioDao.eliminarPorId(usuarioTest.getId());
        }
    }

    @Test
    void testGuardarYBuscarFactura() throws Exception {
        // Verificar que setup creó pedidoTest; si no, fallar temprano
        assertNotNull(pedidoTest, "El pedidoTest no se creó correctamente en setup");
        // Calcular totales para la factura:
        BigDecimal bruto = pedidoTest.getTotal();
        BigDecimal descuentoTotal = BigDecimal.ZERO;
        BigDecimal impuestoTotal = new BigDecimal("10.00");
        // Crear Factura
        Factura factura = new Factura(pedidoTest.getId(), usuarioTest.getId(), bruto, descuentoTotal, impuestoTotal);
        Factura guardada = facturaDao.guardar(factura);
        assertNotNull(guardada.getId(), "El ID de la Factura no debe ser nulo");

        // Buscar y verificar campos
        Optional<Factura> recOpt = facturaDao.buscarPorId(guardada.getId());
        assertTrue(recOpt.isPresent(), "Debe encontrar la factura guardada");
        Factura rec = recOpt.get();
        assertEquals(pedidoTest.getId(), rec.getPedidoId(), "El pedidoId debe coincidir");
        assertEquals(usuarioTest.getId(), rec.getUsuarioId(), "El usuarioId debe coincidir");
        assertEquals(0, rec.getImporteBruto().compareTo(bruto), "Importe bruto coincide");
        BigDecimal esperadoTotal = bruto.subtract(descuentoTotal).add(impuestoTotal);
        assertEquals(0, rec.getImporteTotal().compareTo(esperadoTotal), "Importe total coincide");

        // Limpiar: eliminar la factura creada en este test
        try (var conn = com.tpo.config.SqlServerFactory.getConnection();
             var ps = conn.prepareStatement("DELETE FROM dbo.Facturas WHERE id = ?")) {
            ps.setLong(1, guardada.getId());
            ps.executeUpdate();
        }
    }
}
