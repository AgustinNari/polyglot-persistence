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


}
