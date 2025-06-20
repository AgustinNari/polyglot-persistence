package com.tpo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class SqlServerFactory {
    private static HikariDataSource ds;
    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(AppConfig.get("sql.url"));
        config.setUsername(AppConfig.get("sql.user"));
        config.setPassword(AppConfig.get("sql.pass"));
        config.setMaximumPoolSize(10);
        ds = new HikariDataSource(config);
    }
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
    public static DataSource getDataSource() {
        return ds;
    }
}
