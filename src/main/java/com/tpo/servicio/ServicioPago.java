package com.tpo.servicio;

import com.tpo.dao.PagoDao;
import com.tpo.dao.FacturaDao;
import com.tpo.modelo.pago.Pago;
import com.tpo.modelo.factura.Factura;
import com.tpo.modelo.factura.EstadoFactura;
import com.tpo.modelo.pago.MedioPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ServicioPago {

    private final PagoDao pagoDao;
    private final FacturaDao facturaDao;

    public ServicioPago(PagoDao pagoDao, FacturaDao facturaDao) {
        this.pagoDao = pagoDao;
        this.facturaDao = facturaDao;
    }

    /**
     * Registra un pago para una o varias facturas completas.
     * Monto total debe ser >= suma de importes totales de facturas.
     * Actualiza estado de cada factura a PAGADA.
     */
    public Pago registrarPago(String usuarioId, List<Long> facturaIds, BigDecimal montoTotal, MedioPago medioPago, String operador) throws Exception {
        // Verificar facturas
        BigDecimal sumaFacturas = BigDecimal.ZERO;
        for (Long fid : facturaIds) {
            Optional<Factura> optF = facturaDao.buscarPorId(fid);
            if (optF.isEmpty()) {
                throw new IllegalArgumentException("Factura no encontrada: " + fid);
            }
            Factura f = optF.get();
            if (!f.getEstado().equals(EstadoFactura.PENDIENTE_PAGO)) {
                throw new IllegalStateException("Factura no está en estado PENDIENTE_PAGO: " + fid);
            }
            sumaFacturas = sumaFacturas.add(f.getImporteTotal());
        }
        if (montoTotal.compareTo(sumaFacturas) < 0) {
            throw new IllegalArgumentException("Monto total de pago es menor que la suma de facturas: " + sumaFacturas);
        }
        // Crear Pago
        Pago pago = new Pago();
        pago.setUsuarioId(Long.valueOf(usuarioId));
        pago.setFacturaIds(facturaIds);
        pago.setMontoTotal(montoTotal);
        pago.setMedioPago(medioPago);
        pago.setOperador(operador);
        pago.setFechaPago(LocalDateTime.now());
        Pago guardado = pagoDao.guardar(pago);
        // Actualizar estado de facturas a PAGADA
        for (Long fid : facturaIds) {
            facturaDao.actualizarEstado(fid, EstadoFactura.PAGADA.name());
        }
        return guardado;
    }
}
