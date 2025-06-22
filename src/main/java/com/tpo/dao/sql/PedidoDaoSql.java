package com.tpo.dao.sql;

import com.tpo.dao.PedidoDao;
import com.tpo.modelo.pedido.Pedido;
import com.tpo.modelo.pedido.LineaPedido;
import com.tpo.modelo.pedido.EstadoPedido;
import com.tpo.config.SqlServerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PedidoDaoSql implements PedidoDao {

    @Override
    public Pedido guardar(Pedido pedido) throws Exception {
        String sqlInsertPedido = "INSERT INTO dbo.Pedidos (usuario_id, fecha_creacion, estado, importe_bruto, descuento_total, impuesto_total, importe_total) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = SqlServerFactory.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlInsertPedido, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, pedido.getUsuarioId());

                if (pedido.getFechaCreacion() != null) {
                    ps.setTimestamp(2, Timestamp.valueOf(pedido.getFechaCreacion()));
                } else {
                    ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                }
                ps.setString(3, pedido.getEstado().name());
                ps.setBigDecimal(4, pedido.getImporteBruto());
                ps.setBigDecimal(5, pedido.getDescuentoTotal());
                ps.setBigDecimal(6, pedido.getImpuestoTotal());
                ps.setBigDecimal(7, pedido.getImporteTotal());
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No se insertó el pedido");
                }
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long pedidoId = rs.getLong(1);
                        pedido.setId(pedidoId);
                    } else {
                        throw new SQLException("No se obtuvo ID del pedido insertado");
                    }
                }
            }

            String sqlInsertLinea = "INSERT INTO dbo.LineaPedido (pedido_id, producto_id, cantidad, precio_unitario, descuento, impuesto, subtotal) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psLinea = conn.prepareStatement(sqlInsertLinea, Statement.RETURN_GENERATED_KEYS)) {
                for (LineaPedido lp : pedido.getLineas()) {
                    psLinea.setLong(1, pedido.getId());
                    psLinea.setString(2, lp.getProductoId());
                    psLinea.setInt(3, lp.getCantidad());
                    psLinea.setBigDecimal(4, lp.getPrecioUnitario());
                    psLinea.setBigDecimal(5, lp.getDescuentoLinea());
                    psLinea.setBigDecimal(6, lp.getImpuestoLinea());
                    psLinea.setBigDecimal(7, lp.getSubtotalFinal());
                    int filasL = psLinea.executeUpdate();
                    if (filasL == 0) {
                        throw new SQLException("No se insertó línea de pedido para producto " + lp.getProductoId());
                    }
                    try (ResultSet rsL = psLinea.getGeneratedKeys()) {
                        if (rsL.next()) {
                            long lineaId = rsL.getLong(1);
                            lp.setId(lineaId);
                            lp.setPedidoId(pedido.getId());
                        }
                    }
                }
            }
            conn.commit();
            return pedido;
        } catch (SQLException e) {
            throw e;
        }
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) throws Exception {
        String sqlPedido = "SELECT id, usuario_id, fecha_creacion, estado, importe_bruto, descuento_total, impuesto_total, importe_total " +
                "FROM dbo.Pedidos WHERE id = ?";
        String sqlLineas = "SELECT id, producto_id, cantidad, precio_unitario, descuento, impuesto, subtotal " +
                "FROM dbo.LineaPedido WHERE pedido_id = ?";
        try (Connection conn = SqlServerFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlPedido)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Pedido pedido = new Pedido();
                pedido.setId(rs.getLong("id"));
                pedido.setUsuarioId(rs.getLong("usuario_id"));
                Timestamp ts = rs.getTimestamp("fecha_creacion");
                if (ts != null) {
                    pedido.setFechaCreacion(ts.toLocalDateTime());
                }
                pedido.setEstado(EstadoPedido.valueOf(rs.getString("estado")));
                pedido.setImporteBruto(rs.getBigDecimal("importe_bruto"));
                pedido.setDescuentoTotal(rs.getBigDecimal("descuento_total"));
                pedido.setImpuestoTotal(rs.getBigDecimal("impuesto_total"));
                pedido.setImporteTotal(rs.getBigDecimal("importe_total"));

                try (PreparedStatement psL = conn.prepareStatement(sqlLineas)) {
                    psL.setLong(1, id);
                    try (ResultSet rsL = psL.executeQuery()) {
                        List<LineaPedido> listaLineas = new ArrayList<>();
                        while (rsL.next()) {
                            LineaPedido lp = new LineaPedido();
                            lp.setId(rsL.getLong("id"));
                            lp.setPedidoId(id);
                            lp.setProductoId(rsL.getString("producto_id"));
                            lp.setCantidad(rsL.getInt("cantidad"));
                            lp.setPrecioUnitario(rsL.getBigDecimal("precio_unitario"));
                            lp.setDescuentoLinea(rsL.getBigDecimal("descuento"));
                            lp.setImpuestoLinea(rsL.getBigDecimal("impuesto"));
                            lp.setSubtotalFinal(rsL.getBigDecimal("subtotal"));
                            listaLineas.add(lp);
                        }
                        pedido.setLineas(listaLineas);
                    }
                }
                return Optional.of(pedido);
            }
        }
    }

    @Override
    public void actualizarEstado(Long pedidoId, String nuevoEstado) throws Exception {
        String sql = "UPDATE dbo.Pedidos SET estado = ? WHERE id = ?";
        try (Connection conn = SqlServerFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setLong(2, pedidoId);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se actualizó estado de pedido con id " + pedidoId);
            }
        }
    }


    public List<Pedido> listarPorUsuario(Long usuarioId) throws Exception {
        String sql = "SELECT id, usuario_id, fecha_creacion, estado FROM dbo.Pedidos WHERE usuario_id = ?";
        try (Connection conn = SqlServerFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Pedido> lista = new ArrayList<>();
                while (rs.next()) {
                    Pedido p = new Pedido();
                    p.setId(rs.getLong("id"));
                    p.setUsuarioId(rs.getLong("usuario_id"));
                    p.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                    p.setEstado(EstadoPedido.valueOf(rs.getString("estado")));

                    lista.add(p);
                }
                return lista;
            }
        }
    }

    @Override
    public List<Pedido> listarTodos() throws Exception {
        String sql = "SELECT id FROM dbo.Pedidos";
        List<Pedido> lista = new ArrayList<>();
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
