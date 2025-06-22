package com.tpo.modelo.pedido;

import java.math.BigDecimal;
import java.util.Objects;

public class LineaPedido {
    private Long id;
    private Long pedidoId;
    private String productoId;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal descuentoLinea;
    private BigDecimal impuestoLinea;
    private BigDecimal subtotalFinal;

    public LineaPedido() {}

    public LineaPedido(String productoId, int cantidad, BigDecimal precioUnitario,
                       BigDecimal descuentoLinea, BigDecimal impuestoLinea) {
        setProductoId(productoId);
        setCantidad(cantidad);
        setPrecioUnitario(precioUnitario);
        this.descuentoLinea = (descuentoLinea != null ? descuentoLinea : BigDecimal.ZERO);
        this.impuestoLinea = (impuestoLinea != null ? impuestoLinea : BigDecimal.ZERO);
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
            throw new IllegalArgumentException("Precio unitario inválido");
        }
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getDescuentoLinea() { return descuentoLinea; }
    public void setDescuentoLinea(BigDecimal descuentoLinea) {
        this.descuentoLinea = descuentoLinea != null ? descuentoLinea : BigDecimal.ZERO;
    }

    public BigDecimal getImpuestoLinea() { return impuestoLinea; }
    public void setImpuestoLinea(BigDecimal impuestoLinea) {
        this.impuestoLinea = impuestoLinea != null ? impuestoLinea : BigDecimal.ZERO;
    }

    public void setSubtotalFinal(BigDecimal subtotalFinal) { this.subtotalFinal = subtotalFinal; }
    public BigDecimal getSubtotalFinal() { return subtotalFinal; }
    private void calcularSubtotalFinal() {
        BigDecimal base = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        this.subtotalFinal = base.subtract(descuentoLinea).add(impuestoLinea);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineaPedido)) return false;
        LineaPedido that = (LineaPedido) o;
        return Objects.equals(productoId, that.productoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productoId);
    }

    @Override
    public String toString() {
        return "LineaPedido{" +
                "productoId='" + productoId + '\'' +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", descuentoLinea=" + descuentoLinea +
                ", impuestoLinea=" + impuestoLinea +
                ", subtotalFinal=" + subtotalFinal +
                '}';
    }


    public void recalcularSubtotal() {
        BigDecimal base = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        BigDecimal desc = descuentoLinea != null ? descuentoLinea : BigDecimal.ZERO;
        BigDecimal imp = impuestoLinea != null ? impuestoLinea : BigDecimal.ZERO;
        this.subtotalFinal = base.subtract(desc).add(imp);
    }

}
