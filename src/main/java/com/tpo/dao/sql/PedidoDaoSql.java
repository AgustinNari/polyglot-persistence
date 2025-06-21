package com.tpo.dao.sql;

import com.tpo.dao.PedidoDao;
import com.tpo.modelo.pedido.Pedido;
import com.tpo.modelo.pedido.LineaPedido;
import com.tpo.modelo.pedido.EstadoPedido;
import com.tpo.config.SqlServerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PedidoDaoSql implements PedidoDao {

    @Override
    public Pedido guardar(Pedido pedido) throws Exception {
        // Inserta en tabla Pedidos y luego en LineaPedido dentro de la misma transacción
        String sqlInsertPedido = "INSERT INTO dbo.Pedidos (usuario_id, fecha_creacion, estado) VALUES (?, ?, ?)";
        String sqlInsertLinea = "INSERT INTO dbo.LineaPedido (pedido_id, producto_id, cantidad, precio_unitario, descuento, impuesto, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = SqlServerFactory.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psPedido = conn.prepareStatement(sqlInsertPedido, Statement.RETURN_GENERATED_KEYS)) {
                psPedido.setLong(1, pedido.getUsuarioId());
                psPedido.setTimestamp(2, Timestamp.valueOf(pedido.getFechaCreacion()));
                psPedido.setString(3, pedido.getEstado().name());
                int filas = psPedido.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No se pudo insertar el pedido");
                }
                Long pedidoId;
                try (ResultSet rs = psPedido.getGeneratedKeys()) {
                    if (rs.next()) {
                        pedidoId = rs.getLong(1);
                        pedido.setId(pedidoId);
                    } else {
                        throw new SQLException("No se obtuvo ID al insertar Pedido");
                    }
                }
                // Insertar líneas
                try (PreparedStatement psLinea = conn.prepareStatement(sqlInsertLinea)) {
                    for (LineaPedido linea : pedido.getLineas()) {
                        psLinea.setLong(1, pedidoId);
                        psLinea.setString(2, linea.getProductoId());
                        psLinea.setInt(3, linea.getCantidad());
                        psLinea.setBigDecimal(4, linea.getPrecioUnitario());
                        psLinea.setBigDecimal(5, linea.getDescuentoLinea() != null ? linea.getDescuentoLinea() : java.math.BigDecimal.ZERO);
                        psLinea.setBigDecimal(6, linea.getImpuestoLinea() != null ? linea.getImpuestoLinea() : java.math.BigDecimal.ZERO);
                        psLinea.setBigDecimal(7, linea.getSubtotalFinal());
                        psLinea.addBatch();
                    }
                    psLinea.executeBatch();
                }
                conn.commit();
                return pedido;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
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
                throw new SQLException("No se encontró Pedido con id " + pedidoId);
            }
        }
    }

    @Override
    public Optional<Pedido> buscarPorId(Long pedidoId) throws Exception {
        String sqlPedido = "SELECT id, usuario_id, fecha_creacion, estado FROM dbo.Pedidos WHERE id = ?";
        String sqlLineas = "SELECT producto_id, cantidad, precio_unitario, descuento, impuesto, subtotal FROM dbo.LineaPedido WHERE pedido_id = ?";
        try (Connection conn = SqlServerFactory.getConnection();
             PreparedStatement psPedido = conn.prepareStatement(sqlPedido)) {
            psPedido.setLong(1, pedidoId);
            try (ResultSet rsPedido = psPedido.executeQuery()) {
                if (!rsPedido.next()) {
                    return Optional.empty();
                }
                Pedido pedido = new Pedido();
                pedido.setId(rsPedido.getLong("id"));
                pedido.setUsuarioId(rsPedido.getLong("usuario_id"));
                Timestamp tsCreacion = rsPedido.getTimestamp("fecha_creacion");
                pedido.setFechaCreacion(tsCreacion.toLocalDateTime());
                pedido.setEstado(EstadoPedido.valueOf(rsPedido.getString("estado")));
                // Recuperar líneas
                try (PreparedStatement psLineas = conn.prepareStatement(sqlLineas)) {
                    psLineas.setLong(1, pedidoId);
                    try (ResultSet rsLineas = psLineas.executeQuery()) {
                        while (rsLineas.next()) {
                            LineaPedido linea = new LineaPedido();
                            linea.setProductoId(rsLineas.getString("producto_id"));
                            linea.setCantidad(rsLineas.getInt("cantidad"));
                            linea.setPrecioUnitario(rsLineas.getBigDecimal("precio_unitario"));
                            linea.setDescuentoLinea(rsLineas.getBigDecimal("descuento"));
                            linea.setImpuestoLinea(rsLineas.getBigDecimal("impuesto"));
                            linea.recalcularSubtotal();
                            pedido.agregarLinea(linea);
                        }
                    }
                }
                return Optional.of(pedido);
            }
        }
    }

    @Override
    public List<Pedido> listarPorUsuario(Long usuarioId) throws Exception {
        String sql = "SELECT id FROM dbo.Pedidos WHERE usuario_id = ?";
        List<Pedido> lista = new ArrayList<>();
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
