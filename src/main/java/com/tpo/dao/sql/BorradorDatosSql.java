package com.tpo.dao.sql;
import com.tpo.config.SqlServerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class BorradorDatosSql {
    public static void vaciarTodo() throws Exception {
        try (Connection conn = SqlServerFactory.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                // Orden de DELETE: líneas de pedido, facturas-pagos, pagos, facturas, pedidos, usuarios
                st.executeUpdate("DELETE FROM dbo.LineaPedido");
                st.executeUpdate("DELETE FROM dbo.Factura_Pago");
                st.executeUpdate("DELETE FROM dbo.Pagos");
                st.executeUpdate("DELETE FROM dbo.Facturas");
                st.executeUpdate("DELETE FROM dbo.Pedidos");
                st.executeUpdate("DELETE FROM dbo.Users");
                st.executeUpdate("DBCC CHECKIDENT('dbo.Users', RESEED, 0)");
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}

