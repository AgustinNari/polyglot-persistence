package com.tpo.config;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlIdentifier;

import java.net.InetSocketAddress;

public class CassandraFactory {
    private static final CqlSession session;

    static {
        String host = AppConfig.get("cassandra.contactPoint");
        int port = AppConfig.getInt("cassandra.port");
        String dc = AppConfig.get("cassandra.datacenter");
        String keyspace = AppConfig.get("cassandra.keyspace");


        CqlSession tempSession = CqlSession.builder()
                .addContactPoint(new InetSocketAddress("host.docker.internal", port))
                .withLocalDatacenter(dc)
                .build();


        boolean exists = tempSession.getMetadata()
                .getKeyspaces()
                .containsKey(CqlIdentifier.fromCql(keyspace));
        if (!exists) {
            String createKsCql = String.format(
                    "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy','replication_factor':1} AND durable_writes = true;",
                    keyspace);
            tempSession.execute(createKsCql);
        }


        tempSession.close();


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
