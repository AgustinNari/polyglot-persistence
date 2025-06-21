package com.tpo.servicio;

import com.tpo.dao.SesionUsuarioDao;
import com.tpo.modelo.sesion.SesionUsuario;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ServicioReporte {

    private final SesionUsuarioDao sesionUsuarioDao;

    public ServicioReporte(SesionUsuarioDao sesionUsuarioDao) {
        this.sesionUsuarioDao = sesionUsuarioDao;
    }

    /**
     * Obtiene la cantidad de minutos conectados por día en un rango para un usuario.
     * Devuelve un mapa fecha -> minutos.
     */
    public Map<LocalDate, Long> minutosPorDia(String usuarioId, LocalDate desde, LocalDate hasta) throws Exception {
        Map<LocalDate, Long> mapa = new HashMap<>();
        // Para cada fecha en rango, llamar listarPorUsuarioYRango con fecha=fecha
        LocalDate fecha = desde;
        while (!fecha.isAfter(hasta)) {
            List<SesionUsuario> logs = sesionUsuarioDao.listarPorUsuarioYRango(usuarioId, fecha, fecha);
            long minutosTotales = 0;
            for (SesionUsuario log : logs) {
                LocalDateTime login = log.getFechaLogin();
                LocalDateTime logout = log.getFechaLogout();
                if (logout == null) logout = LocalDateTime.now();
                Duration dur = Duration.between(login, logout);
                minutosTotales += dur.toMinutes();
            }
            mapa.put(fecha, minutosTotales);
            fecha = fecha.plusDays(1);
        }
        return mapa;
    }

    /**
     * Retorna la categoría diaria por día en un rango para un usuario.
     */
    public Map<LocalDate, String> categoriaPorDia(String usuarioId, LocalDate desde, LocalDate hasta) throws Exception {
        Map<LocalDate, String> mapa = new HashMap<>();
        LocalDate fecha = desde;
        while (!fecha.isAfter(hasta)) {
            List<SesionUsuario> logs = sesionUsuarioDao.listarPorUsuarioYRango(usuarioId, fecha, fecha);
            long minutosTotales = 0;
            for (SesionUsuario log : logs) {
                LocalDateTime login = log.getFechaLogin();
                LocalDateTime logout = log.getFechaLogout();
                if (logout == null) logout = LocalDateTime.now();
                Duration dur = Duration.between(login, logout);
                minutosTotales += dur.toMinutes();
            }
            String categoria;
            if (minutosTotales > 240) {
                categoria = "TOP";
            } else if (minutosTotales >= 120) {
                categoria = "MEDIUM";
            } else {
                categoria = "LOW";
            }
            mapa.put(fecha, categoria);
            fecha = fecha.plusDays(1);
        }
        return mapa;
    }
}
