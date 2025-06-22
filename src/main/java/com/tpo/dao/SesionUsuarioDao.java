package com.tpo.dao;

import com.tpo.modelo.sesion.SesionUsuario;

import java.util.List;

public interface SesionUsuarioDao {

    void registrarInicioSesion(SesionUsuario log) throws Exception;

    void registrarLogout(String usuarioId, java.time.LocalDateTime fechaLogoutTime) throws Exception;
    List<SesionUsuario> listarPorUsuarioYRango(String usuarioId, java.time.LocalDate actual) throws Exception;

}
