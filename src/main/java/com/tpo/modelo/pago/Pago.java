package com.tpo.modelo.pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Pago {
    private Long id;
    private Long usuarioId;
    private BigDecimal montoTotal;
    private LocalDateTime fechaPago;
    private MedioPago medioPago;
    private String operador; // puede ser null si no hay operador
    // La relación con facturas se maneja en SQL con tabla intermedia, o aquí como lista:
    private List<Long> facturaIds;

    public Pago() {}

    public Pago(Long usuarioId, BigDecimal montoTotal, MedioPago medioPago, String operador, List<Long> facturaIds) {
        setUsuarioId(usuarioId);
        setMontoTotal(montoTotal);
        setFechaPago(LocalDateTime.now());
        setMedioPago(medioPago);
        setOperador(operador);
        setFacturaIds(facturaIds);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("usuarioId no puede ser nulo");
        }
        this.usuarioId = usuarioId;
    }

    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) {
        if (montoTotal == null || montoTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto total debe ser mayor que cero");
        }
        this.montoTotal = montoTotal;
    }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public MedioPago getMedioPago() { return medioPago; }
    public void setMedioPago(MedioPago medioPago) {
        if (medioPago == null) {
            throw new IllegalArgumentException("medioPago no puede ser nulo");
        }
        this.medioPago = medioPago;
    }

    public String getOperador() { return operador; }
    public void setOperador(String operador) {
        this.operador = operador;
    }

    public List<Long> getFacturaIds() { return facturaIds; }
    public void setFacturaIds(List<Long> facturaIds) {
        if (facturaIds == null || facturaIds.isEmpty()) {
            throw new IllegalArgumentException("Debe incluir al menos una factura para imputar el pago");
        }
        this.facturaIds = facturaIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pago)) return false;
        Pago pago = (Pago) o;
        return Objects.equals(id, pago.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pago{" +
                "id=" + id +
                ", usuarioId=" + usuarioId +
                ", montoTotal=" + montoTotal +
                ", fechaPago=" + fechaPago +
                ", medioPago=" + medioPago +
                '}';
    }
}
