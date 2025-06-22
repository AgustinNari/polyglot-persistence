package com.tpo.modelo.producto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

public class RegistroCambioProducto {
    private String id;
    private String productoId;
    private LocalDateTime fechaCambio;
    private String operador;
    private Map<String, Object> valorAnterior;
    private Map<String, Object> valorNuevo;
    private String tipoOperacion;

    public RegistroCambioProducto() {}

    public RegistroCambioProducto(String productoId, LocalDateTime fechaCambio,
                                  String operador, Map<String, Object> valorAnterior,
                                  Map<String, Object> valorNuevo, String tipoOperacion) {
        this.productoId = productoId;
        this.fechaCambio = fechaCambio;
        this.operador = operador;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        this.tipoOperacion = tipoOperacion;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistroCambioProducto)) return false;
        RegistroCambioProducto that = (RegistroCambioProducto) o;
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        return Objects.equals(productoId, that.productoId)
                && Objects.equals(fechaCambio, that.fechaCambio);
    }

    @Override
    public int hashCode() {
        if (id != null) return Objects.hash(id);
        return Objects.hash(productoId, fechaCambio);
    }

    @Override
    public String toString() {
        return "RegistroCambioProducto{" +
                "productoId='" + productoId + '\'' +
                ", fechaCambio=" + fechaCambio +
                ", operador='" + operador + '\'' +
                ", tipoOperacion='" + tipoOperacion + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getProductoId() {
        return productoId;
    }
    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }
    public LocalDateTime getFechaCambio() {
        return fechaCambio;
    }
    public void setFechaCambio(LocalDateTime fechaCambio) {
        this.fechaCambio = fechaCambio;
    }
    public String getOperador() {
        return operador;
    }
    public void setOperador(String operador) {
        this.operador = operador;
    }
    public Map<String, Object> getValorAnterior() {
        return valorAnterior;
    }
    public void setValorAnterior(Map<String, Object> valorAnterior) {
        this.valorAnterior = valorAnterior;
    }
    public Map<String, Object> getValorNuevo() {
        return valorNuevo;
    }
    public void setValorNuevo(Map<String, Object> valorNuevo) {
        this.valorNuevo = valorNuevo;
    }
    public String getTipoOperacion() {
        return tipoOperacion;
    }
    public void setTipoOperacion(String tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }
}

