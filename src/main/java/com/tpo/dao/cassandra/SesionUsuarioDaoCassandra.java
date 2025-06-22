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

        SimpleStatement stmt = SimpleStatement.builder(
                        "INSERT INTO user_logs (user_id, log_date, login_time, logout_time) VALUES (?, ?, ?, ?)")
                .addPositionalValues(log.getUsuarioId(), logDate, loginInstant, null)
                .build();
        session.execute(stmt);
    }

    @Override
    public void registrarLogout(String usuarioId, LocalDateTime fechaLogoutTime) throws Exception {
        LocalDate logDate = fechaLogoutTime.toLocalDate();


        SimpleStatement selectStmt = SimpleStatement.builder(
                        "SELECT login_time, logout_time FROM user_logs WHERE user_id = ? AND log_date = ?")
                .addPositionalValues(usuarioId, logDate)
                .build();
        ResultSet rs = session.execute(selectStmt);

        Instant loginToUpdate = null;
        for (Row row : rs) {
            Instant logoutInst = row.getInstant("logout_time");
            if (logoutInst == null) {

                Instant candidateLogin = row.getInstant("login_time");
                if (loginToUpdate == null || candidateLogin.isAfter(loginToUpdate)) {
                    loginToUpdate = candidateLogin;
                }
            }
        }

        if (loginToUpdate == null) {

            System.err.println("Warning: no se encontró sesión abierta (logout_time=null) para usuario "
                    + usuarioId + " en fecha " + logDate + ". No se actualiza logout.");
            return;
        }

        Instant logoutInstant = fechaLogoutTime.atZone(ZoneId.systemDefault()).toInstant();


        SimpleStatement updateStmt = SimpleStatement.builder(
                        "UPDATE user_logs SET logout_time = ? WHERE user_id = ? AND log_date = ? AND login_time = ?")
                .addPositionalValues(logoutInstant, usuarioId, logDate, loginToUpdate)
                .build();
        session.execute(updateStmt);
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




}
