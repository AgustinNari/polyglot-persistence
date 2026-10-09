package com.tpo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class SqlServerFactory {
    private static HikariDataSource dataSource;

    static {
        try {
            String url = AppConfig.get("sql.url");
            String user = AppConfig.get("sql.user");
            String pass = AppConfig.getRequiredEnv("MSSQL_SA_PASSWORD");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(pass);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("Error al inicializar SqlServerFactory: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
