package com.tpo.dao.sql;

import com.tpo.dao.UsuarioDao;
import com.tpo.modelo.usuario.CondicionIVA;
import com.tpo.modelo.usuario.RolUsuario;
import com.tpo.modelo.usuario.Usuario;
import com.tpo.config.SqlServerFactory; // asumiendo que factory está en com.tpo.config
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDaoSql implements UsuarioDao {

    @Override
    public Usuario guardar(Usuario u) throws Exception {
        // Sentencia INSERT sin fechaCreacion: la BD asigna DEFAULT SYSUTCDATETIME()
        String sql = "INSERT INTO dbo.Users (nombre, apellido, direccion, docIdentidad, email, contrasena, rol, condicionIVA) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = SqlServerFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getNombre());
            ps.setString(2, u.getApellido());
            ps.setString(3, u.getDireccion());
            ps.setString(4, u.getDocIdentidad());
            ps.setString(5, u.getEmail());
            ps.setString(6, u.getContrasena());
            ps.setString(7, u.getRol().name());
            // condicionIVA: asumimos u.getCondicionIVA() es enum CondicionIVA
            CondicionIVA cond = u.getCondicionIVA();
            if (cond == null) {
                throw new IllegalArgumentException("Condición IVA no puede ser nula");
            }
            ps.setString(8, cond.name());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Crear usuario falló, no se insertó ninguna fila.");
            }
            // Obtener ID generado
            long nuevoId;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    nuevoId = rs.getLong(1);
                    u.setId(nuevoId);
                } else {
                    throw new SQLException("Crear usuario falló, no se obtuvo ID.");
                }
            }
            // Ahora obtenemos fechaCreacion asignada por la BD
            String sql2 = "SELECT fechaCreacion FROM dbo.Users WHERE id = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                ps2.setLong(1, nuevoId);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next()) {
                        Timestamp ts = rs2.getTimestamp("fechaCreacion");
                        if (ts != null) {
                            u.setFechaCreacion(ts.toLocalDateTime());
                        }
                    }
                }
            }
        }
        return u;
    }

    @Override
    public void actualizar(Usuario usuario) throws Exception {
        if (usuario.getId() == null) {
            throw new IllegalArgumentException("El id del usuario es nulo al actualizar");
        }
        String sql = "UPDATE dbo.Users SET nombre = ?, apellido = ?, direccion = ?, docIdentidad = ?, email = ?, contrasena = ?, rol = ?, condicionIVA = ? WHERE id = ?";
        try (Connection conn = SqlServerFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getApellido());
            ps.setString(3, usuario.getDireccion());
            ps.setString(4, usuario.getDocIdentidad());
            ps.setString(5, usuario.getEmail());
            ps.setString(6, usuario.getContrasena());
            ps.setString(7, usuario.getRol().name());
            ps.setString(8, usuario.getCondicionIVA().name());
            ps.setLong(9, usuario.getId());
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se actualizó ningún usuario con id " + usuario.getId());
            }
        }
    }

    @Override
    public void eliminarPorId(Long id) throws Exception {
        String sql = "DELETE FROM dbo.Users WHERE id = ?";
        try (Connection conn = SqlServerFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) throws Exception {
        String sql = "SELECT id, nombre, apellido, direccion, docIdentidad, email, contrasena, rol, condicionIVA, fechaCreacion, total_minutos_actividad FROM dbo.Users WHERE id = ?";
        try (Connection conn = SqlServerFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = mapearUsuario(rs);
                    return Optional.of(u);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> buscarPorDocIdentidad(String docIdentidad) throws Exception {
        String sql = "SELECT id, nombre, apellido, direccion, docIdentidad, email, contrasena, rol, condicionIVA, fechaCreacion, total_minutos_actividad FROM dbo.Users WHERE docIdentidad = ?";
        try (Connection conn = SqlServerFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, docIdentidad);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = mapearUsuario(rs);
                    return Optional.of(u);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Usuario> listarTodos() throws Exception {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, apellido, direccion, docIdentidad, email, contrasena, rol, condicionIVA, fechaCreacion, total_minutos_actividad FROM dbo.Users";
        try (Connection conn = SqlServerFactory.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearUsuario(rs));
            }
        }
        return lista;
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setNombre(rs.getString("nombre"));
        u.setApellido(rs.getString("apellido"));
        u.setDireccion(rs.getString("direccion"));
        u.setDocIdentidad(rs.getString("docIdentidad"));
        u.setEmail(rs.getString("email"));
        u.setContrasena(rs.getString("contrasena"));
        String rolStr = rs.getString("rol");
        try {
            u.setRol(RolUsuario.valueOf(rolStr));
        } catch (Exception e) {
            u.setRol(RolUsuario.CLIENTE); // Valor por defecto o manejar según convenga
        }
        String condStr = rs.getString("condicionIVA");
        CondicionIVA cond;
        if (condStr == null) {
            // Asignar valor por defecto en caso de datos antiguos o NULL
            cond = CondicionIVA.REGIMEN_GENERAL;
        } else {
            try {
                cond = CondicionIVA.valueOf(condStr);
            } catch (Exception e) {
                // Si el valor en BD no coincide con el enum
                cond = CondicionIVA.REGIMEN_GENERAL;
            }
        }
        u.setCondicionIVA(cond);

        Timestamp ts = rs.getTimestamp("fechaCreacion");
        if (ts != null) {
            u.setFechaCreacion(ts.toLocalDateTime());
        }
        // Leer acumulado:
        long totalMin = rs.getLong("total_minutos_actividad");
        u.setTotalMinutosActividad(totalMin);
        return u;
    }


    @Override
    public long contarUsuarios() throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM dbo.Users";
        try (Connection conn = SqlServerFactory.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong("total");
            }
        }
        return 0;
    }

    @Override
    public void incrementarMinutosActividad(Long usuarioId, long minutosMinutos) throws Exception {
        if (usuarioId == null) {
            throw new IllegalArgumentException("ID de usuario nulo");
        }
        if (minutosMinutos <= 0) {
            return; // nada que hacer si es 0 o negativo
        }
        String sql = "UPDATE dbo.Users SET total_minutos_actividad = total_minutos_actividad + ? WHERE id = ?";
        try (Connection conn = SqlServerFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, minutosMinutos);
            ps.setLong(2, usuarioId);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se actualizó total_minutos_actividad para usuario id=" + usuarioId);
            }
        }
    }

}
