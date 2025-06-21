package com.tpo.modelo.pedido;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Pedido {
    private Long id;
    private Long usuarioId;
    private List<LineaPedido> lineas = new ArrayList<>();
    private BigDecimal total; // suma de subtotalFinal de lineas
    private LocalDateTime fechaCreacion;
    private EstadoPedido estado; // enum, por ejemplo: CREADO, FACTURADO, CANCELADO

    public Pedido() {}

    public Pedido(Long usuarioId) {
        setUsuarioId(usuarioId);
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoPedido.CREADO;
        this.total = BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuarioId no puede ser nulo");
        }
        this.usuarioId = usuarioId;
    }

    public List<LineaPedido> getLineas() { return lineas; }
    public void setLineas(List<LineaPedido> lineas) {
        this.lineas = lineas != null ? lineas : new ArrayList<>();
        recalcularTotal();
    }

    public void agregarLinea(LineaPedido linea) {
        if (linea == null) {
            throw new IllegalArgumentException("La línea no puede ser nula");
        }
        this.lineas.add(linea);
        recalcularTotal();
    }

    public void eliminarLinea(String productoId) {
        lineas.removeIf(l -> Objects.equals(l.getProductoId(), productoId));
        recalcularTotal();
    }

    private void recalcularTotal() {
        this.total = lineas.stream()
                .map(LineaPedido::getSubtotalFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() { return total; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pedido)) return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(id, pedido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", usuarioId=" + usuarioId +
                ", total=" + total +
                ", fechaCreacion=" + fechaCreacion +
                ", estado=" + estado +
                '}';
    }
}
