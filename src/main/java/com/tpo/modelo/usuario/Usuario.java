package com.tpo.modelo.usuario;

import com.tpo.modelo.usuario.RolUsuario;

import java.time.LocalDateTime;
import java.util.Objects;

public class Usuario {
    private Long id;
    private String nombre;
    private String apellido;
    private String direccion;
    private String docIdentidad;
    private String email;
    private String contrasena;
    private RolUsuario rol;
    private LocalDateTime fechaCreacion;
    private long totalMinutosActividad;  // acumulado

    // (Opcional) condición ante IVA
    private CondicionIVA condicionIVA;

    public Usuario() {
        // Por defecto rol será asignado en el servicio
    }

    // Constructor para crear: sin id, pero con rol asignado en servicio
    public Usuario(String nombre, String apellido, String direccion,
                   String docIdentidad, String email, String contrasena, RolUsuario rol) {
        setNombre(nombre);
        setApellido(apellido);
        setDireccion(direccion);
        setDocIdentidad(docIdentidad);
        setEmail(email);
        setContrasena(contrasena);
        setRol(rol);
        this.fechaCreacion = LocalDateTime.now(); // fecha de creación por defecto
        // condición IVA por defecto si se desea
        this.condicionIVA = null;
    }

    // Getters y setters con validaciones ligeras
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public long getTotalMinutosActividad() { return totalMinutosActividad; }
    public void setTotalMinutosActividad(long totalMinutosActividad) {
        this.totalMinutosActividad = totalMinutosActividad;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre no puede ser vacío");
        }
        this.nombre = nombre.trim();
    }

    public String getApellido() {
        return apellido;
    }
    public void setApellido(String apellido) {
        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("Apellido no puede ser vacío");
        }
        this.apellido = apellido.trim();
    }

    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String direccion) {
        if (direccion == null || direccion.isBlank()) {
            throw new IllegalArgumentException("Dirección no puede ser vacía");
        }
        this.direccion = direccion.trim();
    }

    public String getDocIdentidad() {
        return docIdentidad;
    }
    public void setDocIdentidad(String docIdentidad) {
        if (docIdentidad == null || docIdentidad.isBlank()) {
            throw new IllegalArgumentException("Documento de identidad no puede ser vacío");
        }
        // Podrías agregar validación de patrón: e.g. sólo dígitos o con guiones
        this.docIdentidad = docIdentidad.trim();
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email no puede ser vacío");
        }
        this.email = email.trim();
    }

    public String getContrasena() {
        return contrasena;
    }
    public void setContrasena(String contrasena) {
        if (contrasena == null) {
            throw new IllegalArgumentException("Contraseña no puede ser nula");
        }
        this.contrasena = contrasena;
    }

    public RolUsuario getRol() {
        return this.rol;
    }
    public void setRol(RolUsuario rol) {
        if (rol == null) {
            throw new IllegalArgumentException("Rol no puede ser nulo");
        }
        this.rol = rol;
    }


    public CondicionIVA getCondicionIVA() {
        return condicionIVA;
    }
    public void setCondicionIVA(CondicionIVA condicionIVA) {
        this.condicionIVA = condicionIVA;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", docIdentidad='" + docIdentidad + '\'' +
                ", email='" + email + '\'' +
                ", rol=" + rol + '\'' +
                ", condicionIVA=" + condicionIVA + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", totalMinutosActividad=" + totalMinutosActividad +
                '}';
    }
}
