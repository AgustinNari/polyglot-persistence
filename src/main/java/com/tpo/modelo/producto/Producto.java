package com.tpo.modelo.producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Producto {
    private String id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private List<String> urlsFotos = new ArrayList<>();
    private List<String> urlsVideos = new ArrayList<>();
    private List<String> comentarios = new ArrayList<>();
    private List<String> etiquetas = new ArrayList<>();
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public Producto() {}

    public Producto(String nombre, String descripcion, BigDecimal precio) {
        setNombre(nombre);
        setDescripcion(descripcion);
        setPrecio(precio);
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de producto no puede ser nulo o vacío");
        }
        this.nombre = nombre;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser nulo o negativo");
        }
        this.precio = precio;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public List<String> getUrlsFotos() { return urlsFotos; }
    public void setUrlsFotos(List<String> urlsFotos) {
        this.urlsFotos = urlsFotos != null ? urlsFotos : new ArrayList<>();
        this.fechaActualizacion = LocalDateTime.now();
    }

    public List<String> getUrlsVideos() { return urlsVideos; }
    public void setUrlsVideos(List<String> urlsVideos) {
        this.urlsVideos = urlsVideos != null ? urlsVideos : new ArrayList<>();
        this.fechaActualizacion = LocalDateTime.now();
    }

    public List<String> getComentarios() { return comentarios; }
    public void setComentarios(List<String> comentarios) {
        this.comentarios = comentarios != null ? comentarios : new ArrayList<>();
        this.fechaActualizacion = LocalDateTime.now();
    }

    public List<String> getEtiquetas() { return etiquetas; }
    public void setEtiquetas(List<String> etiquetas) {
        this.etiquetas = etiquetas != null ? etiquetas : new ArrayList<>();
        this.fechaActualizacion = LocalDateTime.now();
    }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto)) return false;
        Producto producto = (Producto) o;
        if (id != null && producto.id != null) {
            return Objects.equals(id, producto.id);
        }
        return Objects.equals(nombre, producto.nombre);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(nombre);
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                '}';
    }
}
