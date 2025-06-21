package com.tpo.dao;
import com.tpo.dao.sql.FacturaDaoSql;
import com.tpo.dao.sql.PagoDaoSql;
import com.tpo.dao.sql.PedidoDaoSql;
import com.tpo.dao.sql.UsuarioDaoSql;

public class DaoFactory {


    // Usuarios
    public static UsuarioDao crearUsuarioDao() {
        return new UsuarioDaoSql();
    }

    // Pedidos
    public static PedidoDao crearPedidoDao() {
        return new PedidoDaoSql();
    }

    // Facturas
    public static FacturaDao crearFacturaDao() {
        return new FacturaDaoSql();
    }

    // Pagos
    public static PagoDao crearPagoDao() {
        return new PagoDaoSql();
    }

    // Mongo DAOs
    public static com.tpo.dao.ProductoDao crearProductoDaoMongo() {
        return new com.tpo.dao.mongo.ProductoDaoMongo();
    }
    public static com.tpo.dao.RegistroCambioProductoDao crearRegistroCambioProductoDaoMongo() {
        return new com.tpo.dao.mongo.RegistroCambioProductoDaoMongo();
    }

    // Redis DAO
    public static com.tpo.dao.CarritoDao crearCarritoDaoRedis() {
        return new com.tpo.dao.redis.CarritoDaoRedis();
    }

    // Cassandra DAO
    public static com.tpo.dao.SesionUsuarioDao crearSesionUsuarioDaoCassandra() {
        return new com.tpo.dao.cassandra.SesionUsuarioDaoCassandra();
    }



}
