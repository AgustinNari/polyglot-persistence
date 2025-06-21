package com.tpo.dao.mongo;

import com.tpo.dao.ProductoDao;
import com.tpo.modelo.producto.Producto;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductoDaoMongoTest {

    private ProductoDao productoDao;

    @BeforeAll
    void setup() {
        productoDao = new ProductoDaoMongo();
        // Asegúrate de que Mongo está levantado y la base 'tpo' existe con colección products.
    }

    @Test
    void testGuardarYBuscarYActualizarYEliminar() throws Exception {
        // Crear producto de prueba
        Producto p = new Producto("ProductoTest", "Descripción test", new BigDecimal("123.45"));
        p.getUrlsFotos().add("http://foto1.jpg");
        p.getUrlsVideos().add("http://video1.mp4");
        p.getComentarios().add("Comentario inicial");
        p.getEtiquetas().add("test");
        Producto guardado = productoDao.guardar(p);
        assertNotNull(guardado.getId());

        // Buscar por ID
        Optional<Producto> recOpt = productoDao.buscarPorId(guardado.getId());
        assertTrue(recOpt.isPresent());
        Producto rec = recOpt.get();
        assertEquals("ProductoTest", rec.getNombre());

        // Actualizar: cambiar precio y descripción
        rec.setPrecio(new BigDecimal("150.00"));
        rec.setDescripcion("Descripción modificada");
        productoDao.actualizar(rec);

        // Buscar de nuevo y verificar
        Optional<Producto> rec2Opt = productoDao.buscarPorId(rec.getId());
        assertTrue(rec2Opt.isPresent());
        Producto rec2 = rec2Opt.get();
        assertEquals(0, rec2.getPrecio().compareTo(new BigDecimal("150.00")));
        assertEquals("Descripción modificada", rec2.getDescripcion());

        // Buscar por nombre (regex)
        List<Producto> encontrados = productoDao.buscarPorNombre("ProductoTest");
        assertTrue(encontrados.stream().anyMatch(prod -> prod.getId().equals(rec2.getId())));

        // Eliminar
        productoDao.eliminarPorId(rec2.getId());
        Optional<Producto> rec3Opt = productoDao.buscarPorId(rec2.getId());
        assertFalse(rec3Opt.isPresent());
    }
}
