package com.tpo.dao.sql;

import com.tpo.dao.PagoDao;
import com.tpo.modelo.pago.MedioPago;
import com.tpo.modelo.pago.Pago;
import com.tpo.config.SqlServerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PagoDaoSql implements PagoDao {

    @Override
    public Pago guardar(Pago pago) throws Exception {
        String sqlInsertPago = "INSERT INTO dbo.Pagos (usuario_id, monto_total, fecha_pago, medio_pago, operador) VALUES (?, ?, ?, ?, ?)";
        String sqlInsertFacturaPago = "INSERT INTO dbo.Factura_Pago (pago_id, factura_id) VALUES (?, ?)";
        try (Connection conn = SqlServerFactory.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psPago = conn.prepareStatement(sqlInsertPago, Statement.RETURN_GENERATED_KEYS)) {
                psPago.setLong(1, pago.getUsuarioId());
                psPago.setBigDecimal(2, pago.getMontoTotal());
                psPago.setTimestamp(3, Timestamp.valueOf(pago.getFechaPago()));
                psPago.setString(4, pago.getMedioPago().name());
                psPago.setString(5, pago.getOperador());
                int filas = psPago.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No se insertó el pago");
                }
                Long pagoId;
                try (ResultSet rs = psPago.getGeneratedKeys()) {
                    if (rs.next()) {
                        pagoId = rs.getLong(1);
                        pago.setId(pagoId);
                    } else {
                        throw new SQLException("No se obtuvo ID al insertar Pago");
                    }
                }

                try (PreparedStatement psFP = conn.prepareStatement(sqlInsertFacturaPago)) {
                    for (Long facturaId : pago.getFacturaIds()) {
                        psFP.setLong(1, pago.getId());
                        psFP.setLong(2, facturaId);
                        psFP.addBatch();
                    }
                    psFP.executeBatch();
                }
                conn.commit();
                return pago;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public Optional<Pago> buscarPorId(Long pagoId) throws Exception {
        String sqlPago = "SELECT usuario_id, monto_total, fecha_pago, medio_pago, operador FROM dbo.Pagos WHERE id = ?";
        String sqlFacturas = "SELECT factura_id FROM dbo.Factura_Pago WHERE pago_id = ?";
        try (Connection conn = SqlServerFactory.getConnection();
             PreparedStatement psPago = conn.prepareStatement(sqlPago)) {
            psPago.setLong(1, pagoId);
            try (ResultSet rs = psPago.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Pago pago = new Pago();
                pago.setId(pagoId);
                pago.setUsuarioId(rs.getLong("usuario_id"));
                pago.setMontoTotal(rs.getBigDecimal("monto_total"));
                pago.setFechaPago(rs.getTimestamp("fecha_pago").toLocalDateTime());
                pago.setMedioPago(com.tpo.modelo.pago.MedioPago.valueOf(rs.getString("medio_pago")));
                pago.setOperador(rs.getString("operador"));

                try (PreparedStatement psF = conn.prepareStatement(sqlFacturas)) {
                    psF.setLong(1, pagoId);
                    try (ResultSet rsFP = psF.executeQuery()) {
                        List<Long> listaFacturas = new ArrayList<>();
                        while (rsFP.next()) {
                            listaFacturas.add(rsFP.getLong("factura_id"));
                        }
                        pago.setFacturaIds(listaFacturas);
                    }
                }
                return Optional.of(pago);
            }
        }
    }

    @Override
    public List<Pago> listarPorUsuario(Long usuarioId) throws Exception {
        String sql = "SELECT id, usuario_id, monto_total, fecha_pago, medio_pago, operador FROM dbo.Pagos WHERE usuario_id = ?";
        try (Connection conn = SqlServerFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Pago> lista = new ArrayList<>();
                while (rs.next()) {
                    Pago p = new Pago();
                    p.setId(rs.getLong("id"));
                    p.setUsuarioId(rs.getLong("usuario_id"));
                    p.setMontoTotal(rs.getBigDecimal("monto_total"));
                    p.setFechaPago(rs.getTimestamp("fecha_pago").toLocalDateTime());
                    p.setMedioPago(MedioPago.valueOf(rs.getString("medio_pago")));
                    p.setOperador(rs.getString("operador"));
                    lista.add(p);
                }
                return lista;
            }
        }
    }


    @Override
    public List<Pago> listarTodos() throws Exception {
        String sql = "SELECT id FROM dbo.Pagos";
        List<Pago> lista = new ArrayList<>();
        try (Connection conn = SqlServerFactory.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Long id = rs.getLong("id");
                buscarPorId(id).ifPresent(lista::add);
            }
        }
        return lista;
    }
}
