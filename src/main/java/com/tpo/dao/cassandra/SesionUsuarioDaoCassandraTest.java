package com.tpo.dao.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.tpo.config.CassandraFactory;
import com.tpo.dao.SesionUsuarioDao;
import com.tpo.modelo.sesion.SesionUsuario;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SesionUsuarioDaoCassandraTest {

    private SesionUsuarioDao sesionUsuarioDao;
    private final String usuarioId = "user-cass-test";

    @BeforeAll
    void setup() throws Exception {
        sesionUsuarioDao = new SesionUsuarioDaoCassandra();
        CqlSession session = CassandraFactory.getSession();
        // Primero, eliminar la tabla si existe:
        session.execute("DROP TABLE IF EXISTS user_logs");
        // Luego, crear la tabla con clave de partición y clustering apropiados:
        session.execute(
                "CREATE TABLE IF NOT EXISTS user_logs ("
                        + "user_id text, "
                        + "log_date date, "
                        + "login_time timestamp, "
                        + "logout_time timestamp, "
                        + "PRIMARY KEY (user_id, log_date, login_time)"
                        + ") WITH CLUSTERING ORDER BY (log_date DESC, login_time DESC)"
        );
    }


    @Test
    void testRegistrarYListarSesionUsuario() throws Exception {
        // Registrar inicio de sesión
        LocalDateTime loginTime = LocalDateTime.now().withNano(0);
        SesionUsuario logIni = new SesionUsuario();
        logIni.setUsuarioId(usuarioId);
        logIni.setFechaLogin(loginTime);
        sesionUsuarioDao.registrarInicioSesion(logIni);

        // Simular logout unos minutos después
        LocalDateTime logoutTime = loginTime.plusMinutes(5);
        LocalDate fechaLoginDate = loginTime.toLocalDate();
        sesionUsuarioDao.registrarLogout(usuarioId, fechaLoginDate, loginTime, logoutTime);

        // Listar logs en rango de hoy
        LocalDate hoy = LocalDate.now();
        List<SesionUsuario> lista = sesionUsuarioDao.listarPorUsuarioYRango(usuarioId, hoy);
        boolean encontrado = lista.stream().anyMatch(l ->
                l.getFechaLogin().equals(loginTime) &&
                        l.getFechaLogout() != null &&
                        l.getFechaLogout().equals(logoutTime)
        );
        assertTrue(encontrado, "Debe encontrar el LogSesion insertado y actualizado");
    }
}
