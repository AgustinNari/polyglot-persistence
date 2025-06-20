package com.tpo.config;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CassandraTest {
    @Test
    void testConnection() {
        CqlSession session = CassandraFactory.getSession();
        Row row = session.execute("SELECT release_version FROM system.local").one();
        assertNotNull(row.getString("release_version"));
    }
}
