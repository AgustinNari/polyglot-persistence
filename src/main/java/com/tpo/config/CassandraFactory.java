package com.tpo.config;

import com.datastax.oss.driver.api.core.CqlSession;

import java.net.InetSocketAddress;

public class CassandraFactory {
    private static CqlSession session;
    static {
        String host = AppConfig.get("cassandra.contactPoint");
        int port = Integer.parseInt(AppConfig.get("cassandra.port"));
        String dc = AppConfig.get("cassandra.datacenter");
        String keyspace = AppConfig.get("cassandra.keyspace");
        session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(host, port))
                .withLocalDatacenter(dc)
                .withKeyspace(keyspace)
                .build();
    }
    public static CqlSession getSession() {
        return session;
    }
}
