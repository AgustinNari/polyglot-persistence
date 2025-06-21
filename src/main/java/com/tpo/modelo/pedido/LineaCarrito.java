package com.tpo.modelo.pedido;

import java.math.BigDecimal;
import java.util.Objects;

public class LineaCarrito {
    private String productoId;
    private int cantidad;
    private BigDecimal precioUnitario;
    // Nuevo campo para almacenar subtotal
    private BigDecimal subtotal;

    public LineaCarrito() {}

    public LineaCarrito(String productoId, int cantidad, BigDecimal precioUnitario) {
        setProductoId(productoId);
        setCantidad(cantidad);
        setPrecioUnitario(precioUnitario);
        recalcularSubtotal();
    }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) {
        if (productoId == null || productoId.isBlank()) {
            throw new IllegalArgumentException("El ID de producto no puede ser nulo o vacío");
        }
        this.productoId = productoId;
    }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser nulo o negativo");
        }
        this.precioUnitario = precioUnitario;
    }

    /**
     * Retorna el subtotal: precioUnitario * cantidad.
     * Si el campo subtotal es null, lo recalcula.
     */
    public BigDecimal getSubtotal() {
        if (subtotal == null && precioUnitario != null) {
            return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        }
        return subtotal;
    }

    /**
     * Recalcula y actualiza el campo subtotal en base a precioUnitario y cantidad.
     * Debe invocarse después de cambiar cantidad o precioUnitario.
     */
    public void recalcularSubtotal() {
        if (precioUnitario == null) {
            throw new IllegalStateException("No se puede recalcular subtotal sin precioUnitario");
        }
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineaCarrito)) return false;
        LineaCarrito that = (LineaCarrito) o;
        return Objects.equals(productoId, that.productoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productoId);
    }

    @Override
    public String toString() {
        return "LineaCarrito{" +
                "productoId='" + productoId + '\'' +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}
