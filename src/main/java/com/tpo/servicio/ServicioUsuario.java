package com.tpo.servicio;

import com.tpo.dao.UsuarioDao;
import com.tpo.dao.SesionUsuarioDao;
import com.tpo.modelo.sesion.SesionUsuario;
import com.tpo.modelo.usuario.CategoriaUsuario;
import com.tpo.modelo.usuario.RolUsuario;
import com.tpo.modelo.usuario.Usuario;


import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class ServicioUsuario {

    private final UsuarioDao usuarioDao;
    private final SesionUsuarioDao sesionUsuarioDao;


    private static Map<String, SesionUsuario> sesionesActivas = new HashMap<>();

    public ServicioUsuario(UsuarioDao usuarioDao, SesionUsuarioDao sesionUsuarioDao) {
        this.usuarioDao = usuarioDao;
        this.sesionUsuarioDao = sesionUsuarioDao;
    }


    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );
    private static final int MIN_PASSWORD_LENGTH = 6;


    public Usuario registrarUsuario(Usuario u) throws Exception {

        if (u.getNombre() == null || u.getNombre().isBlank()) {
            throw new IllegalArgumentException("Nombre requerido");
        }
        if (u.getEmail() == null || !u.getEmail().matches(".+@.+\\..+")) {
            throw new IllegalArgumentException("Email inválido");
        }
        if (u.getContrasena() == null || u.getContrasena().length() < 6) {
            throw new IllegalArgumentException("Contraseña debe tener al menos 6 caracteres");
        }

        Optional<Usuario> existente = usuarioDao.buscarPorDocIdentidad(u.getDocIdentidad());
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe usuario con ese DocIdentidad");
        }

        long total = usuarioDao.contarUsuarios();
        if (total == 0) {
            u.setRol(RolUsuario.ADMIN);
        } else {
            u.setRol(RolUsuario.CLIENTE);
        }

        if (u.getCondicionIVA() == null) {
            throw new IllegalArgumentException("Condición IVA requerida");
        }

        Usuario creado = usuarioDao.guardar(u);
        return creado;
    }

    public Usuario login(String docIdentidad, String contrasena) throws Exception {
        Optional<Usuario> opt = usuarioDao.buscarPorDocIdentidad(docIdentidad);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado con docIdentidad: " + docIdentidad);
        }
        Usuario u = opt.get();

        if (!u.getContrasena().equals(contrasena)) {
            throw new IllegalArgumentException("Contraseña incorrecta");
        }

        String userIdStr = String.valueOf(u.getId());


        SesionUsuario log = new SesionUsuario();
        log.setUsuarioId(userIdStr);
        log.setFechaLogin(LocalDateTime.now());
        sesionUsuarioDao.registrarInicioSesion(log);
        sesionesActivas.put(userIdStr, log);

        return u;
    }

    public Usuario buscarUsuarioPorId(Long id) throws Exception {
        if (id == null) {
            throw new IllegalArgumentException("ID de usuario no puede ser nulo");
        }
        Optional<Usuario> opt = usuarioDao.buscarPorId(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + id);
        }
        return opt.get();
    }


    public void logout(Usuario u, LocalDateTime fechaLoginTime) throws Exception {
        String userIdStr = String.valueOf(u.getId());
        SesionUsuario log = sesionesActivas.get(userIdStr);
        if (log != null) {
            LocalDate fechaLoginDate = fechaLoginTime.toLocalDate();
            LocalDateTime fechaLogoutTime = LocalDateTime.now();
            sesionUsuarioDao.registrarLogout(userIdStr, fechaLogoutTime);
            sesionesActivas.remove(userIdStr);


            long minutosSesion = Duration.between(fechaLoginTime, fechaLogoutTime).toMinutes();
            if (minutosSesion > 0) {

                usuarioDao.incrementarMinutosActividad(u.getId(), minutosSesion);

                long acumuladoAnterior = u.getTotalMinutosActividad();
                u.setTotalMinutosActividad(acumuladoAnterior + minutosSesion);
            }
        }
    }




    public CategoriaUsuario obtenerCategoriaPromedio(Usuario usuario) throws Exception {
        if (usuario.getId() == null) {
            throw new IllegalArgumentException("Usuario sin ID para calcular categoría");
        }
        if (usuario.getFechaCreacion() == null) {
            throw new IllegalStateException("Usuario sin fechaCreacion");
        }

        long totalMinutos = usuario.getTotalMinutosActividad();

        LocalDate fechaInicio = usuario.getFechaCreacion().toLocalDate();
        LocalDate fechaFin = LocalDate.now();
        long diasTranscurridos = java.time.Duration.between(fechaInicio.atStartOfDay(), fechaFin.atStartOfDay()).toDays() + 1;
        if (diasTranscurridos <= 0) {
            diasTranscurridos = 1;
        }

        double promedio = (double) totalMinutos / diasTranscurridos;
        System.out.println("Total de minutos acumulados: " + totalMinutos);
        System.out.println("Días transcurridos: " + diasTranscurridos);
        System.out.println("Promedio diario (min): " + promedio);
        if (promedio >= 240.0) {
            return CategoriaUsuario.TOP;
        } else if (promedio >= 120.0) {
            return CategoriaUsuario.MEDIUM;
        } else {
            return CategoriaUsuario.LOW;
        }
    }



    public List<SesionUsuario> listarTodasSesiones(Usuario u) throws Exception {
        if (u.getId() == null || u.getFechaCreacion() == null) {
            throw new IllegalArgumentException("Usuario o fechaCreacion nulos");
        }
        LocalDate desde = u.getFechaCreacion().toLocalDate();
        LocalDate hasta = LocalDate.now();
        return sesionUsuarioDao.listarPorUsuarioYRango(String.valueOf(u.getId()), LocalDate.now());
    }



}

