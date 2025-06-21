package com.tpo.dao.sql;

import com.tpo.dao.FacturaDao;
import com.tpo.modelo.factura.Factura;
import com.tpo.modelo.factura.EstadoFactura;
import com.tpo.config.SqlServerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FacturaDaoSql implements FacturaDao {

    @Override
    public Factura guardar(Factura factura) throws Exception {
        String sqlInsert = "INSERT INTO dbo.Facturas (pedido_id, usuario_id, importe_bruto, descuento_total, impuesto_total, importe_total, fecha_emision, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = SqlServerFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, factura.getPedidoId());
            ps.setLong(2, factura.getUsuarioId());
            ps.setBigDecimal(3, factura.getImporteBruto());
            ps.setBigDecimal(4, factura.getDescuentoTotal());
            ps.setBigDecimal(5, factura.getImpuestoTotal());
            ps.setBigDecimal(6, factura.getImporteTotal());
            ps.setTimestamp(7, Timestamp.valueOf(factura.getFechaEmision()));
            ps.setString(8, factura.getEstado().name());
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se insertó la factura");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    factura.setId(rs.getLong(1));
                }
            }
            return factura;
        }
    }

    @Override
    public void actualizarEstado(Long facturaId, String nuevoEstado) throws Exception {
        String sql = "UPDATE dbo.Facturas SET estado = ? WHERE id = ?";
        try (Connection conn = SqlServerFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setLong(2, facturaId);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se encontró Factura con id " + facturaId);
            }
        }
    }

    @Override
    public Optional<Factura> buscarPorId(Long facturaId) throws Exception {
        String sql = "SELECT id, pedido_id, usuario_id, importe_bruto, descuento_total, impuesto_total, importe_total, fecha_emision, estado FROM dbo.Facturas WHERE id = ?";
        try (Connection conn = SqlServerFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, facturaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Factura f = mapearFactura(rs);
                    return Optional.of(f);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Factura> listarPorUsuario(Long usuarioId) throws Exception {
        String sql = "SELECT id FROM dbo.Facturas WHERE usuario_id = ?";
        List<Factura> lista = new ArrayList<>();
        try (Connection conn = SqlServerFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    buscarPorId(id).ifPresent(lista::add);
                }
            }
        }
        return lista;
    }

    @Override
    public List<Factura> listarPendientesPorUsuario(Long usuarioId) throws Exception {
        String sql = "SELECT id FROM dbo.Facturas WHERE usuario_id = ? AND estado = ?";
        List<Factura> lista = new ArrayList<>();
        try (Connection conn = SqlServerFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            ps.setString(2, EstadoFactura.PENDIENTE_PAGO.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    buscarPorId(id).ifPresent(lista::add);
                }
            }
        }
        return lista;
    }

    @Override
    public List<Factura> listarTodas() throws Exception {
        String sql = "SELECT id FROM dbo.Facturas";
        List<Factura> lista = new ArrayList<>();
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

    private Factura mapearFactura(ResultSet rs) throws SQLException {
        Factura f = new Factura();
        f.setId(rs.getLong("id"));
        f.setPedidoId(rs.getLong("pedido_id"));
        f.setUsuarioId(rs.getLong("usuario_id"));
        f.setImporteBruto(rs.getBigDecimal("importe_bruto"));
        f.setDescuentoTotal(rs.getBigDecimal("descuento_total"));
        f.setImpuestoTotal(rs.getBigDecimal("impuesto_total"));
        f.setImporteTotal(rs.getBigDecimal("importe_total"));
        Timestamp ts = rs.getTimestamp("fecha_emision");
        f.setFechaEmision(ts.toLocalDateTime());
        f.setEstado(EstadoFactura.valueOf(rs.getString("estado")));
        return f;
    }
}
