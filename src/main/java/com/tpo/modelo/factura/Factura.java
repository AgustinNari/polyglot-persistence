package com.tpo.modelo.factura;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Factura {
    private Long id;
    private Long pedidoId;
    private Long usuarioId;
    private BigDecimal importeBruto; // suma de líneas sin descuento ni impuestos
    private BigDecimal descuentoTotal;
    private BigDecimal impuestoTotal;
    private BigDecimal importeTotal; // importeBruto - descuentoTotal + impuestoTotal
    private LocalDateTime fechaEmision;
    private EstadoFactura estado; // PENDIENTE_PAGO, PARCIALMENTE_PAGADA, PAGADA

    public Factura() {}

    public Factura(Long pedidoId, Long usuarioId, BigDecimal importeBruto,
                   BigDecimal descuentoTotal, BigDecimal impuestoTotal) {
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
        this.importeBruto = importeBruto != null ? importeBruto : BigDecimal.ZERO;
        this.descuentoTotal = descuentoTotal != null ? descuentoTotal : BigDecimal.ZERO;
        this.impuestoTotal = impuestoTotal != null ? impuestoTotal : BigDecimal.ZERO;
        this.importeTotal = this.importeBruto.subtract(this.descuentoTotal).add(this.impuestoTotal);
        this.fechaEmision = LocalDateTime.now();
        this.estado = EstadoFactura.PENDIENTE_PAGO;
    }

    // Getters y setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public BigDecimal getImporteBruto() { return importeBruto; }
    public void setImporteBruto(BigDecimal importeBruto) { this.importeBruto = importeBruto; }

    public BigDecimal getDescuentoTotal() { return descuentoTotal; }
    public void setDescuentoTotal(BigDecimal descuentoTotal) { this.descuentoTotal = descuentoTotal; }

    public BigDecimal getImpuestoTotal() { return impuestoTotal; }
    public void setImpuestoTotal(BigDecimal impuestoTotal) { this.impuestoTotal = impuestoTotal; }

    public BigDecimal getImporteTotal() { return importeTotal; }
    public void setImporteTotal(BigDecimal importeTotal) { this.importeTotal = importeTotal; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    public EstadoFactura getEstado() { return estado; }
    public void setEstado(EstadoFactura estado) { this.estado = estado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Factura)) return false;
        Factura factura = (Factura) o;
        return Objects.equals(id, factura.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Factura{" +
                "id=" + id +
                ", pedidoId=" + pedidoId +
                ", usuarioId=" + usuarioId +
                ", importeTotal=" + importeTotal +
                ", fechaEmision=" + fechaEmision +
                ", estado=" + estado +
                '}';
    }
}
