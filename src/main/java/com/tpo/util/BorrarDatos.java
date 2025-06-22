package com.tpo.util;



import com.tpo.config.SqlServerFactory;
import com.tpo.config.MongoFactory;
import com.tpo.config.CassandraFactory;
import com.tpo.config.RedisFactory;


import java.sql.Connection;


public class BorrarDatos {
    public static void main(String[] args) {
        try {
            vaciarSQL();
            vaciarMongo();
            vaciarRedis();
            vaciarCassandra();
            System.out.println("Todas las Bases de Datos vaciadas.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    public static void vaciarSQL() throws Exception {
        try (Connection conn = SqlServerFactory.getConnection()) {
            conn.setAutoCommit(false);
            try (var st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM dbo.LineaPedido");
                st.executeUpdate("DELETE FROM dbo.Factura_Pago");
                st.executeUpdate("DELETE FROM dbo.Pagos");
                st.executeUpdate("DELETE FROM dbo.Facturas");
                st.executeUpdate("DELETE FROM dbo.Pedidos");
                st.executeUpdate("DELETE FROM dbo.Users");
                st.executeUpdate("DBCC CHECKIDENT('dbo.Users', RESEED, 0)");
                conn.commit();
                System.out.println("SQL: Tablas vaciadas");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static void vaciarMongo() {
        var client = MongoFactory.getClient();
        var db = client.getDatabase(MongoFactory.getDatabaseName());
        db.getCollection("products").deleteMany(new org.bson.Document());
        db.getCollection("product_history").deleteMany(new org.bson.Document());
        System.out.println("Mongo: colecciones vaciadas");
    }


    public static void vaciarRedis() {
        try (var jedis = RedisFactory.getConnection()) {
            jedis.flushDB();
            System.out.println("Redis: vaciado");
        }
    }

    public static void vaciarCassandra() {
        var session = CassandraFactory.getSession();
        session.execute("TRUNCATE tpo.user_logs");
        System.out.println("Cassandra: user_logs truncada");
    }

}
