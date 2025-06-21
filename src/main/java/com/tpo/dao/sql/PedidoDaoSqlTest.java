package com.tpo.dao.sql;

import com.tpo.modelo.pedido.Pedido;
import com.tpo.modelo.pedido.LineaPedido;
import com.tpo.modelo.usuario.Usuario;
import com.tpo.modelo.usuario.RolUsuario;
import org.junit.jupiter.api.*;


import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PedidoDaoSqlTest {

    private UsuarioDaoSql usuarioDao;
    private PedidoDaoSql pedidoDao;

    private Usuario usuarioTest;

    @BeforeAll
    void setup() throws Exception {
        usuarioDao = new UsuarioDaoSql();
        pedidoDao = new PedidoDaoSql();
        // Crear un usuario temporal
        Usuario u = new Usuario("TestPedido", "Usuario", "Calle Test", "DNI-PEDIDO-TEST", "testpedido@example.com", "passTest", RolUsuario.CLIENTE);
        usuarioTest = usuarioDao.guardar(u);
    }

    @AfterAll
    void teardown() throws Exception {
        // Eliminar usuario temporal
        if (usuarioTest != null && usuarioTest.getId() != null) {
            usuarioDao.eliminarPorId(usuarioTest.getId());
        }
    }

    @Test
    void testGuardarYBuscarPedido() throws Exception {
        Long usuarioId = usuarioTest.getId();
        // Crear un Pedido con 2 líneas simuladas
        Pedido pedido = new Pedido(usuarioId);
        LineaPedido linea1 = new LineaPedido("prod-1", 2, new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("5.00"));
        linea1.recalcularSubtotal();
        LineaPedido linea2 = new LineaPedido("prod-2", 1, new BigDecimal("50.00"), BigDecimal.ZERO, new BigDecimal("2.50"));
        linea2.recalcularSubtotal();
        pedido.agregarLinea(linea1);
        pedido.agregarLinea(linea2);

        // Guardar
        Pedido guardado = pedidoDao.guardar(pedido);
        assertNotNull(guardado.getId(), "El ID de Pedido no debe ser nulo");

        // Buscar
        Optional<Pedido> recOpt = pedidoDao.buscarPorId(guardado.getId());
        assertTrue(recOpt.isPresent(), "Debe encontrar el pedido guardado");
        Pedido rec = recOpt.get();
        assertEquals(usuarioId, rec.getUsuarioId());
        assertEquals(2, rec.getLineas().size(), "Debe tener 2 líneas");
        BigDecimal totalEsperado = linea1.getSubtotalFinal().add(linea2.getSubtotalFinal());
        assertEquals(0, rec.getImporteTotal().compareTo(totalEsperado), "El total debe coincidir");

        // Limpiar: eliminar pedido y sus líneas.
        // Como no tenemos método eliminar en DAO, podemos:
        // - En test, directamente ejecutar DELETE. O bien implementas método eliminar en DAO.
        // Para simplicidad de test, ejecuta:
        //   DELETE FROM dbo.LineaPedido WHERE pedido_id = guardado.getId();
        //   DELETE FROM dbo.Pedidos WHERE id = guardado.getId();
        // A continuación un ejemplo usando JDBC:
        try (var conn = com.tpo.config.SqlServerFactory.getConnection();
             var ps1 = conn.prepareStatement("DELETE FROM dbo.LineaPedido WHERE pedido_id = ?");
             var ps2 = conn.prepareStatement("DELETE FROM dbo.Pedidos WHERE id = ?")) {
            ps1.setLong(1, guardado.getId());
            ps1.executeUpdate();
            ps2.setLong(1, guardado.getId());
            ps2.executeUpdate();
        }
    }
}
