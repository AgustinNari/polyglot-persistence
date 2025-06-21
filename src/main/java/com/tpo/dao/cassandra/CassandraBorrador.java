package com.tpo.dao.cassandra;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.CqlSession;
import com.tpo.config.CassandraFactory;

public class CassandraBorrador {
    public static void vaciarUserLogs() {
        CqlSession session = CassandraFactory.getSession();
        // TRUNCATE es más rápido
        session.execute("TRUNCATE tpo.user_logs");
        System.out.println("Cassandra user_logs truncada.");
    }
}
