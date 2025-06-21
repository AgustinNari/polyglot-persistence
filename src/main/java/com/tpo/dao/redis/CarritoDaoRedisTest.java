package com.tpo.dao.redis;

import com.tpo.dao.CarritoDao;
import com.tpo.modelo.pedido.LineaCarrito;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CarritoDaoRedisTest {

    private CarritoDao carritoDao;
    private final String usuarioId = "usuario-test-redis";

    @BeforeAll
    void setup() throws Exception {
        carritoDao = new CarritoDaoRedis();
        // Inicializar carrito vacío
        carritoDao.eliminarCarrito(usuarioId);
        carritoDao.crearCarrito(usuarioId);
    }

    @AfterAll
    void teardown() throws Exception {
        carritoDao.eliminarCarrito(usuarioId);
    }

    @Test
    void testAgregarYObtenerLineas() throws Exception {
        // Asegurarse de carrito limpio
        carritoDao.limpiarCarrito(usuarioId);
        LineaCarrito linea1 = new LineaCarrito("prod-redis-1", 2, new BigDecimal("50.00"));
        linea1.recalcularSubtotal();
        carritoDao.agregarLinea(usuarioId, linea1);

        List<LineaCarrito> lineas = carritoDao.obtenerLineas(usuarioId);
        assertEquals(1, lineas.size());
        LineaCarrito rec = lineas.get(0);
        assertEquals("prod-redis-1", rec.getProductoId());
        assertEquals(2, rec.getCantidad());
    }

    @Test
    void testActualizarYEliminarLinea() throws Exception {
        carritoDao.limpiarCarrito(usuarioId);
        LineaCarrito linea = new LineaCarrito("prod-redis-2", 1, new BigDecimal("30.00"));
        linea.recalcularSubtotal();
        carritoDao.agregarLinea(usuarioId, linea);

        // Actualizar: cambiar cantidad
        linea.setCantidad(3);
        linea.recalcularSubtotal();
        carritoDao.actualizarLinea(usuarioId, linea);

        List<LineaCarrito> lineas = carritoDao.obtenerLineas(usuarioId);
        assertEquals(1, lineas.size());
        assertEquals(3, lineas.get(0).getCantidad());

        // Eliminar
        carritoDao.eliminarLinea(usuarioId, "prod-redis-2");
        List<LineaCarrito> lineas2 = carritoDao.obtenerLineas(usuarioId);
        assertTrue(lineas2.isEmpty());
    }

    @Test
    void testUndoRedo() throws Exception {
        carritoDao.limpiarCarrito(usuarioId);
        // Agregar dos líneas secuencialmente
        LineaCarrito l1 = new LineaCarrito("A", 1, new BigDecimal("10.00"));
        l1.recalcularSubtotal();
        carritoDao.agregarLinea(usuarioId, l1);

        LineaCarrito l2 = new LineaCarrito("B", 2, new BigDecimal("5.00"));
        l2.recalcularSubtotal();
        carritoDao.agregarLinea(usuarioId, l2);

        // Estado actual: A y B
        List<LineaCarrito> estado = carritoDao.obtenerLineas(usuarioId);
        assertEquals(2, estado.size());

        // Deshacer última acción (agregar B): debería quedar sólo A
        assertTrue(carritoDao.puedeDeshacer(usuarioId));
        carritoDao.deshacer(usuarioId);
        List<LineaCarrito> afterUndo = carritoDao.obtenerLineas(usuarioId);
        assertEquals(1, afterUndo.size());
        assertEquals("A", afterUndo.get(0).getProductoId());

        // Rehacer: recuperar B
        assertTrue(carritoDao.puedeRehacer(usuarioId));
        carritoDao.rehacer(usuarioId);
        List<LineaCarrito> afterRedo = carritoDao.obtenerLineas(usuarioId);
        assertEquals(2, afterRedo.size());
        // Podría no garantizar orden; verificar presencia de ambos:
        boolean tieneA = afterRedo.stream().anyMatch(l -> l.getProductoId().equals("A"));
        boolean tieneB = afterRedo.stream().anyMatch(l -> l.getProductoId().equals("B"));
        assertTrue(tieneA && tieneB);
    }
}
