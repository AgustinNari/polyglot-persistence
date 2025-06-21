package com.tpo.servicio;

import com.tpo.dao.UsuarioDao;
import com.tpo.dao.SesionUsuarioDao;
import com.tpo.modelo.sesion.SesionUsuario;
import com.tpo.modelo.usuario.RolUsuario;
import com.tpo.modelo.usuario.Usuario;


import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class ServicioUsuario {

    private final UsuarioDao usuarioDao;
    private final SesionUsuarioDao sesionUsuarioDao;

    public ServicioUsuario(UsuarioDao usuarioDao, SesionUsuarioDao sesionUsuarioDao) {
        this.usuarioDao = usuarioDao;
        this.sesionUsuarioDao = sesionUsuarioDao;
    }

    // Patrón simple de email; puedes ajustar o usar librería externa si fuera necesario
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * Registra un nuevo usuario con validaciones:
     * - email en formato válido
     * - contraseña longitud mínima
     * - docIdentidad único
     * - nombre, apellido, dirección no vacíos (el POJO ya valida no vacíos)
     * - rol: si es el primer usuario en la base, asignar ADMIN, sino CLIENTE
     */
    public Usuario registrarUsuario(Usuario u) throws Exception {
        // Validar email
        String email = u.getEmail();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email con formato inválido");
        }
        // Validar contraseña
        String pass = u.getContrasena();
        if (pass.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres");
        }
        // Validar docIdentidad único
        Optional<Usuario> existente = usuarioDao.buscarPorDocIdentidad(u.getDocIdentidad());
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con docIdentidad: " + u.getDocIdentidad());
        }


        // Asignar rol: si no hay usuarios aún, primer usuario es ADMIN; si hay al menos 1, es CLIENTE
        long total = usuarioDao.contarUsuarios();
        if (total == 0) {
            u.setRol(RolUsuario.ADMIN);
        } else {
            u.setRol(RolUsuario.CLIENTE);
        }

        // Guardar en BD
        return usuarioDao.guardar(u);
    }

    /**
     * Login del usuario: verifica credenciales, registra inicio de sesión en Cassandra y devuelve el Usuario.
     */
    public Usuario login(String docIdentidad, String contrasena) throws Exception {
        Optional<Usuario> opt = usuarioDao.buscarPorDocIdentidad(docIdentidad);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado con docIdentidad: " + docIdentidad);
        }
        Usuario u = opt.get();
        // En un escenario real: comparar hashed passwords
        if (!u.getContrasena().equals(contrasena)) {
            throw new IllegalArgumentException("Contraseña incorrecta");
        }
        // Registrar inicio de sesión
        SesionUsuario log = new SesionUsuario();
        log.setUsuarioId(String.valueOf(u.getId()));
        log.setFechaLogin(LocalDateTime.now());
        sesionUsuarioDao.registrarInicioSesion(log);
        return u;
    }

    /**
     * Logout: actualiza la última sesión sin logout_time en Cassandra.
     * Para ello, necesitamos saber la hora de login.
     * En este ejemplo, suponemos que la aplicación retiene la última hora de login para el usuario.
     * Podrías pasar como parámetro la fechaLoginTime obtenida en el login.
     */
    public void logout(Usuario u, LocalDateTime fechaLoginTime) throws Exception {
        LocalDate fechaDate = fechaLoginTime.toLocalDate();
        LocalDateTime fechaLogout = LocalDateTime.now();
        sesionUsuarioDao.registrarLogout(String.valueOf(u.getId()), fechaDate, fechaLoginTime, fechaLogout);
    }

    /**
     * Calcula la categoría del usuario para una fecha dada:
     * Suma todos los minutos de sesiones ese día y categoriza TOP (>240), MEDIUM (120-240], LOW (<120).
     * Opcional: guardar el resultado en SQL o cache en Redis.
     */
    public String calcularCategoriaDiaria(Usuario u, LocalDate fecha) throws Exception {
        List<SesionUsuario> logs = sesionUsuarioDao.listarPorUsuarioYRango(
                String.valueOf(u.getId()), fecha, fecha);
        long minutosTotales = 0;
        for (SesionUsuario log : logs) {
            LocalDateTime login = log.getFechaLogin();
            LocalDateTime logout = log.getFechaLogout();
            if (logout == null) {
                logout = LocalDateTime.now();
            }
            Duration dur = Duration.between(login, logout);
            minutosTotales += dur.toMinutes();
        }
        if (minutosTotales > 240) {
            return "TOP";
        } else if (minutosTotales >= 120) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    // Puedes agregar otros métodos: obtener perfil, actualizar datos de usuario, etc.
}
