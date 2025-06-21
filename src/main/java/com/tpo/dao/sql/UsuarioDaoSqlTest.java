package com.tpo.dao.sql;

import com.tpo.modelo.usuario.RolUsuario;
import com.tpo.modelo.usuario.Usuario;
import org.junit.jupiter.api.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsuarioDaoSqlTest {

    private UsuarioDaoSql usuarioDao;

    @BeforeAll
    void setup() {
        usuarioDao = new UsuarioDaoSql();
    }

    @Test
    void testGuardarYBuscar() throws Exception {
        Usuario u = new Usuario("Juan", "Pérez", "Calle Falsa 123", "DNI12345", "juan@example.com", "password123", RolUsuario.CLIENTE);
        Usuario guardado = usuarioDao.guardar(u);
        assertNotNull(guardado.getId(), "El id generado no debe ser nulo");

        Optional<Usuario> recuperado = usuarioDao.buscarPorId(guardado.getId());
        assertTrue(recuperado.isPresent(), "Debe encontrar el usuario por id");
        assertEquals("Juan", recuperado.get().getNombre());
        // Limpieza: eliminar al final
        usuarioDao.eliminarPorId(guardado.getId());
    }

    @Test
    void testActualizar() throws Exception {
        Usuario u = new Usuario("María", "Gómez", "Av. Siempre Viva 456", "DNI67890", "maria@example.com", "pass456", RolUsuario.CLIENTE);
        Usuario guardado = usuarioDao.guardar(u);
        Long id = guardado.getId();

        guardado.setDireccion("Nueva Dirección 789");
        usuarioDao.actualizar(guardado);

        Optional<Usuario> rec = usuarioDao.buscarPorId(id);
        assertTrue(rec.isPresent());
        assertEquals("Nueva Dirección 789", rec.get().getDireccion());

        usuarioDao.eliminarPorId(id);
    }
}
