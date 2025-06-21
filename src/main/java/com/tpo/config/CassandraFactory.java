package com.tpo.config;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;

import java.net.InetSocketAddress;

public class CassandraFactory {
    private static final CqlSession session;

    static {
        String host = AppConfig.get("cassandra.contactPoint");
        int port = AppConfig.getInt("cassandra.port");
        String dc = AppConfig.get("cassandra.datacenter");
        String keyspace = AppConfig.get("cassandra.keyspace");

        // 1. Crear una sesión inicial sin keyspace
        CqlSession tempSession = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(host, port))
                .withLocalDatacenter(dc)
                .build();

        // 2. Verificar si el keyspace existe; si no, crearlo
        boolean exists = tempSession.getMetadata()
                .getKeyspaces()
                .containsKey(CqlIdentifier.fromCql(keyspace));
        if (!exists) {
            String createKsCql = String.format(
                    "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy','replication_factor':1} AND durable_writes = true;",
                    keyspace);
            tempSession.execute(createKsCql);
        }

        // 3. Cerrar la sesión temporal
        tempSession.close();

        // 4. Crear la sesión definitiva con keyspace
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
