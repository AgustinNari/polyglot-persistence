package com.tpo.dao.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.tpo.dao.SesionUsuarioDao;
import com.tpo.modelo.sesion.SesionUsuario;
import com.tpo.config.CassandraFactory;
import com.datastax.oss.driver.api.core.cql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class SesionUsuarioDaoCassandra implements SesionUsuarioDao {

    private final CqlSession session;

    public SesionUsuarioDaoCassandra() {
        session = CassandraFactory.getSession();
    }

    @Override
    public void registrarInicioSesion(SesionUsuario log) throws Exception {
        LocalDateTime fechaLogin = log.getFechaLogin();
        LocalDate logDate = fechaLogin.toLocalDate();
        Instant loginInstant = fechaLogin.atZone(ZoneId.systemDefault()).toInstant();
        // INSERT con Instant; el driver mapea Instant -> timestamp de Cassandra
        SimpleStatement stmt = SimpleStatement.builder(
                        "INSERT INTO user_logs (user_id, log_date, login_time, logout_time) VALUES (?, ?, ?, ?)")
                .addPositionalValues(log.getUsuarioId(), logDate, loginInstant, null)
                .build();
        session.execute(stmt);
    }

    @Override
    public void registrarLogout(String usuarioId, LocalDate fechaLoginDate, LocalDateTime fechaLoginTime, LocalDateTime fechaLogoutTime) throws Exception {
        Instant loginInstant = fechaLoginTime.atZone(ZoneId.systemDefault()).toInstant();
        Instant logoutInstant = fechaLogoutTime.atZone(ZoneId.systemDefault()).toInstant();
        SimpleStatement stmt = SimpleStatement.builder(
                        "UPDATE user_logs SET logout_time = ? WHERE user_id = ? AND log_date = ? AND login_time = ?")
                .addPositionalValues(logoutInstant, usuarioId, fechaLoginDate, loginInstant)
                .build();
        session.execute(stmt);
    }

    @Override
    public List<SesionUsuario> listarPorUsuarioYRango(String usuarioId, LocalDate actual) throws Exception {
        List<SesionUsuario> lista = new ArrayList<>();
        SimpleStatement stmt = SimpleStatement.builder(
                        "SELECT user_id, log_date, login_time, logout_time FROM user_logs WHERE user_id = ? AND log_date = ?")
                .addPositionalValues(usuarioId, actual)
                .build();
        ResultSet rs = session.execute(stmt);
        for (Row row : rs) {
            SesionUsuario log = new SesionUsuario();
            log.setUsuarioId(row.getString("user_id"));
            LocalDate logDate = row.getLocalDate("log_date");
            log.setFechaLogin(LocalDateTime.ofInstant(row.getInstant("login_time"), ZoneId.systemDefault()));
            Instant logoutInstant = row.getInstant("logout_time");
            if (logoutInstant != null) {
                log.setFechaLogout(LocalDateTime.ofInstant(logoutInstant, ZoneId.systemDefault()));
            }
            lista.add(log);
        }
        return lista;
    }

    @Override
    public long sumarMinutosActividadDesde(String userId, LocalDateTime desde) {
        LocalDate startDate = desde.toLocalDate();
        LocalDate today = LocalDate.now();
        long totalMinutos = 0L;

        for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
            SimpleStatement stmt = SimpleStatement.builder(
                            "SELECT login_time, logout_time FROM user_logs WHERE user_id = ? AND log_date = ?")
                    .addPositionalValues(userId, date)
                    .build();
            ResultSet rs = session.execute(stmt);
            int filas = 0;
            for (Row row : rs) {
                filas++;
                Instant loginInstant = row.getInstant("login_time");
                Instant logoutInstant = row.getInstant("logout_time");
                if (loginInstant != null && logoutInstant != null) {
                    long diffMin = Duration.between(loginInstant, logoutInstant).toMinutes();
                    if (diffMin > 0) {
                        totalMinutos += diffMin;
                    }
                }
            }
            System.out.println("[DEBUG] Fecha " + date + ": filas encontradas = " + filas);
        }
        System.out.println("[DEBUG] TotalMinutos calculado: " + totalMinutos);
        return totalMinutos;
    }


}
