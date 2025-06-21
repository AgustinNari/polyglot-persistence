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

    // Map para almacenar la sesión activa por usuario
    private static Map<String, SesionUsuario> sesionesActivas = new HashMap<>();

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
        // Validaciones:
        if (u.getNombre() == null || u.getNombre().isBlank()) {
            throw new IllegalArgumentException("Nombre requerido");
        }
        if (u.getEmail() == null || !u.getEmail().matches(".+@.+\\..+")) {
            throw new IllegalArgumentException("Email inválido");
        }
        if (u.getContrasena() == null || u.getContrasena().length() < 6) {
            throw new IllegalArgumentException("Contraseña debe tener al menos 6 caracteres");
        }
        // Verificar docIdentidad único:
        Optional<Usuario> existente = usuarioDao.buscarPorDocIdentidad(u.getDocIdentidad());
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe usuario con ese DocIdentidad");
        }
        // Asignar rol por defecto (si es el primer usuario, podrías asignar ADMIN, sino CLIENTE):
        long total = usuarioDao.contarUsuarios();
        if (total == 0) {
            u.setRol(RolUsuario.ADMIN);
        } else {
            u.setRol(RolUsuario.CLIENTE);
        }
        // Condición IVA ya debe estar seteada antes de llamar aquí.
        if (u.getCondicionIVA() == null) {
            throw new IllegalArgumentException("Condición IVA requerida");
        }
        // Llamar DAO:
        Usuario creado = usuarioDao.guardar(u);
        return creado;
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

        String userIdStr = String.valueOf(u.getId());

        // Registrar inicio de sesión
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

    /**
     * Logout: actualiza la última sesión sin logout_time en Cassandra.
     * Para ello, necesitamos saber la hora de login.
     * En este ejemplo, suponemos que la aplicación retiene la última hora de login para el usuario.
     * Podrías pasar como parámetro la fechaLoginTime obtenida en el login.
     */
    public void logout(Usuario u, LocalDateTime fechaLoginTime) throws Exception {
        String userIdStr = String.valueOf(u.getId());
        SesionUsuario log = sesionesActivas.get(userIdStr);
        if (log != null) {
            LocalDate fechaLoginDate = fechaLoginTime.toLocalDate();
            LocalDateTime fechaLogoutTime = LocalDateTime.now();
            sesionUsuarioDao.registrarLogout(userIdStr, fechaLoginDate, fechaLoginTime, fechaLogoutTime);
            sesionesActivas.remove(userIdStr);

            // 1) Calcular duración de esta sesión:
            long minutosSesion = Duration.between(fechaLoginTime, fechaLogoutTime).toMinutes();
            if (minutosSesion > 0) {
                // 2) Incrementar acumulado en SQL:
                usuarioDao.incrementarMinutosActividad(u.getId(), minutosSesion);
                // 3) Actualizar la propiedad en el objeto Usuario en memoria (opcional):
                long acumuladoAnterior = u.getTotalMinutosActividad();
                u.setTotalMinutosActividad(acumuladoAnterior + minutosSesion);
            }
        }
    }


        /**
         * Calcula la categoría del usuario para una fecha dada:
         * Suma todos los minutos de sesiones ese día y categoriza TOP (>240), MEDIUM (120-240], LOW (<120).
         * Opcional: guardar el resultado en SQL o cache en Redis.
         */
        public String calcularCategoriaDiaria (Usuario u, LocalDate fecha) throws Exception {
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
            System.out.println("Minutos totales: " + minutosTotales);
            if (minutosTotales > 240) {
                return "TOP";
            } else if (minutosTotales >= 120) {
                return "MEDIUM";
            } else {
                return "LOW";
            }
        }

    /**
     * Suma los minutos de actividad desde la fecha de creación hasta hoy, iterando día a día.
     * Reutiliza la lógica de calcularCategoriaDiaria para cada día.
     * @return total minutos sumados en todo el rango.
     */
    private long sumarMinutosDesdeFechaCreacion(Usuario usuario) throws Exception {
        LocalDate fechaInicio = usuario.getFechaCreacion().toLocalDate();
        LocalDate fechaFin = LocalDate.now();
        long totalMinutos = 0;
        for (LocalDate fecha = fechaInicio; !fecha.isAfter(fechaFin); fecha = fecha.plusDays(1)) {
            // Para cada día, sumar minutos de sesiones de ese día:
            List<SesionUsuario> logs = sesionUsuarioDao.listarPorUsuarioYRango(
                    String.valueOf(usuario.getId()), fecha, fecha);
            long minutosDia = 0;
            for (SesionUsuario log : logs) {
                LocalDateTime login = log.getFechaLogin();
                LocalDateTime logout = log.getFechaLogout();
                if (login == null) {
                    continue;
                }
                if (logout == null) {
                    logout = LocalDateTime.now().withNano(0);
                }
                Duration dur = Duration.between(login, logout);
                long mins = dur.toMinutes();
                minutosDia += mins;
            }
            System.out.println("[DEBUG] sumarMinutosDesdeFechaCreacion - Usuario " + usuario.getId() +
                    ", fecha " + fecha + ", minutosDia=" + minutosDia);
            totalMinutos += minutosDia;
        }
        System.out.println("[DEBUG] sumarMinutosDesdeFechaCreacion - Usuario " + usuario.getId() +
                ", totalMinutos=" + totalMinutos);
        return totalMinutos/60; // Convertir a minutos
    }

    /**
     * Obtiene la categoría promedio del usuario calculando el promedio diario de minutos de actividad
     * desde su fecha de creación hasta hoy.
     * @return TOP, MEDIUM o LOW según promedio diario (>=240 TOP; >=120 MEDIUM; <120 LOW)
     */
    public CategoriaUsuario obtenerCategoriaPromedio(Usuario usuario) throws Exception {
        if (usuario.getId() == null) {
            throw new IllegalArgumentException("Usuario sin ID para calcular categoría");
        }
        if (usuario.getFechaCreacion() == null) {
            throw new IllegalStateException("Usuario sin fechaCreacion");
        }
        // 1. Leer total de minutos acumulados desde la base SQL (propiedad en objeto Usuario):
        long totalMinutos = usuario.getTotalMinutosActividad();
        // 2. Calcular días transcurridos:
        LocalDate fechaInicio = usuario.getFechaCreacion().toLocalDate();
        LocalDate fechaFin = LocalDate.now();
        long diasTranscurridos = java.time.Duration.between(fechaInicio.atStartOfDay(), fechaFin.atStartOfDay()).toDays() + 1;
        if (diasTranscurridos <= 0) {
            diasTranscurridos = 1;
        }
        // 3. Promedio:
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


    public List<SesionUsuario> listarSesionesPorRango(Usuario u, LocalDate desde, LocalDate hasta) throws Exception {
        String userIdStr = String.valueOf(u.getId());
        return sesionUsuarioDao.listarPorUsuarioYRango(userIdStr, desde, hasta);
    }

    public List<SesionUsuario> listarTodasSesiones(Usuario u) throws Exception {
        if (u.getId() == null || u.getFechaCreacion() == null) {
            throw new IllegalArgumentException("Usuario o fechaCreacion nulos");
        }
        LocalDate desde = u.getFechaCreacion().toLocalDate();
        LocalDate hasta = LocalDate.now();
        return sesionUsuarioDao.listarPorUsuarioYRango(String.valueOf(u.getId()), desde, hasta);
    }




    public List<SesionUsuario> listarSesionesActivas( String usuarioId) throws Exception {
        return sesionUsuarioDao.listarPorUsuarioYRango(usuarioId, LocalDate.now(), LocalDate.now()); }


}

