package com.tpo.dao.mongo;

import com.tpo.dao.RegistroCambioProductoDao;
import com.tpo.modelo.producto.RegistroCambioProducto;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RegistroCambioProductoDaoMongoTest {

    private RegistroCambioProductoDao registroDao;

    @BeforeAll
    void setup() {
        registroDao = new RegistroCambioProductoDaoMongo();
    }

    @Test
    void testGuardarYListar() throws Exception {
        String productoId = "prod-history-test";
        LocalDateTime ahora = LocalDateTime.now();
        String operador = "usuarioTest";
        Map<String, Object> anterior = new HashMap<>();
        anterior.put("nombre", "OldName");
        Map<String, Object> nuevo = new HashMap<>();
        nuevo.put("nombre", "NewName");

        RegistroCambioProducto reg = new RegistroCambioProducto(productoId, ahora, operador, anterior, nuevo, "MODIFICAR");
        RegistroCambioProducto guardado = registroDao.guardar(reg);
        assertNotNull(guardado.getId());

        List<RegistroCambioProducto> lista = registroDao.listarPorProducto(productoId);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(r -> r.getId().equals(guardado.getId())));

        // No eliminamos aquí; la colección puede acumular registros de prueba o limpiarse manualmente.
    }
}
