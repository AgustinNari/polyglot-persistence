package com.tpo.modelo.sesion;


import java.time.LocalDateTime;
import java.util.Objects;

public class SesionUsuario {
    private Long id; // si en Cassandra no usas id, puedes omitirlo en POJO
    private String usuarioId;
    private LocalDateTime fechaLogin;
    private LocalDateTime fechaLogout;

    public SesionUsuario() {}

    public SesionUsuario(String usuarioId, LocalDateTime fechaLogin) {
        setUsuarioId(usuarioId);
        setFechaLogin(fechaLogin);
    }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new IllegalArgumentException("usuarioId no puede ser nulo");
        }
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getFechaLogin() { return fechaLogin; }
    public void setFechaLogin(LocalDateTime fechaLogin) {
        this.fechaLogin = fechaLogin;
    }

    public LocalDateTime getFechaLogout() { return fechaLogout; }
    public void setFechaLogout(LocalDateTime fechaLogout) {
        this.fechaLogout = fechaLogout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SesionUsuario)) return false;
        SesionUsuario that = (SesionUsuario) o;
        return Objects.equals(usuarioId, that.usuarioId) &&
                Objects.equals(fechaLogin, that.fechaLogin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuarioId, fechaLogin);
    }

    @Override
    public String toString() {
        return "LogSesion{" +
                "usuarioId='" + usuarioId + '\'' +
                ", fechaLogin=" + fechaLogin +
                ", fechaLogout=" + fechaLogout +
                '}';
    }
}
