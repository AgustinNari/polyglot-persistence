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
    private LocalDateTime fechaCreacion;
    private EstadoPedido estado;


    private BigDecimal importeBruto;
    private BigDecimal descuentoTotal;
    private BigDecimal impuestoTotal;
    private BigDecimal importeTotal;


    public Pedido() {}

    public Pedido(Long usuarioId) {
        this.usuarioId = usuarioId;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoPedido.CREADO;
        this.importeBruto = BigDecimal.ZERO;
        this.descuentoTotal = BigDecimal.ZERO;
        this.impuestoTotal = BigDecimal.ZERO;
        this.importeTotal = BigDecimal.ZERO;
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

    public void agregarLinea(LineaPedido lp) {
        if (lp == null) {
            throw new IllegalArgumentException("La línea de pedido no puede ser nula");
        }

        lp.recalcularSubtotal();


        BigDecimal baseLinea = lp.getPrecioUnitario().multiply(BigDecimal.valueOf(lp.getCantidad()));


        if (importeBruto == null) importeBruto = BigDecimal.ZERO;
        if (descuentoTotal == null) descuentoTotal = BigDecimal.ZERO;
        if (impuestoTotal == null) impuestoTotal = BigDecimal.ZERO;
        if (importeTotal == null) importeTotal = BigDecimal.ZERO;


        importeBruto = importeBruto.add(baseLinea);
        descuentoTotal = descuentoTotal.add(lp.getDescuentoLinea());
        impuestoTotal = impuestoTotal.add(lp.getImpuestoLinea());

        importeTotal = importeBruto.subtract(descuentoTotal).add(impuestoTotal);


        lineas.add(lp);
    }

    public void eliminarLinea(String productoId) {
        lineas.removeIf(l -> Objects.equals(l.getProductoId(), productoId));
        recalcularTotal();
    }

    private void recalcularTotal() {
        BigDecimal sumaBase = BigDecimal.ZERO;
        BigDecimal sumaDesc = BigDecimal.ZERO;
        BigDecimal sumaImp = BigDecimal.ZERO;
        for (LineaPedido lp : lineas) {
            BigDecimal baseLinea = lp.getPrecioUnitario().multiply(BigDecimal.valueOf(lp.getCantidad()));
            sumaBase = sumaBase.add(baseLinea);
            sumaDesc = sumaDesc.add(lp.getDescuentoLinea());
            sumaImp = sumaImp.add(lp.getImpuestoLinea());
        }
        this.importeBruto = sumaBase;
        this.descuentoTotal = sumaDesc;
        this.impuestoTotal = sumaImp;
        this.importeTotal = importeBruto.subtract(descuentoTotal).add(impuestoTotal);
    }


    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    public BigDecimal getImporteBruto() {
        return importeBruto;
    }
    public void setImporteBruto(BigDecimal importeBruto) {
        this.importeBruto = importeBruto != null ? importeBruto : BigDecimal.ZERO;
    }
    public BigDecimal getDescuentoTotal() {
        return descuentoTotal;
    }
    public void setDescuentoTotal(BigDecimal descuentoTotal) {
        this.descuentoTotal = descuentoTotal != null ? descuentoTotal : BigDecimal.ZERO;
    }
    public BigDecimal getImpuestoTotal() {
        return impuestoTotal;
    }
    public void setImpuestoTotal(BigDecimal impuestoTotal) {
        this.impuestoTotal = impuestoTotal != null ? impuestoTotal : BigDecimal.ZERO;
    }
    public BigDecimal getImporteTotal() {
        return importeTotal;
    }
    public void setImporteTotal(BigDecimal importeTotal) {
        this.importeTotal = importeTotal != null ? importeTotal : BigDecimal.ZERO;
    }

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
                ", fechaCreacion=" + fechaCreacion +
                ", estado=" + estado +
                ", importeBruto=" + importeBruto +
                ", descuentoTotal=" + descuentoTotal +
                ", impuestoTotal=" + impuestoTotal +
                ", importeTotal=" + importeTotal +
                ", lineas=" + lineas +
                '}';
    }
}
