package com.tpo.dao;

import com.tpo.modelo.sesion.SesionUsuario;
import java.time.LocalDate;
import java.util.List;

public interface SesionUsuarioDao {
    // Registrar inicio de sesión: retorna el LogSesion con fechaLogin
    void registrarInicioSesion(SesionUsuario log) throws Exception;
    // Registrar logout: usa mismo usuarioId y fechaLogin para actualizar logout_time
    void registrarLogout(String usuarioId, java.time.LocalDate fechaLoginDate, java.time.LocalDateTime fechaLoginTime, java.time.LocalDateTime fechaLogoutTime) throws Exception;
    List<SesionUsuario> listarPorUsuarioYRango(String usuarioId, java.time.LocalDate desde, java.time.LocalDate hasta) throws Exception;
}
