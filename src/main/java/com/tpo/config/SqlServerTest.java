package com.tpo.config;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SqlServerTest {
    @Test
    void testConnection() throws Exception {
        try (Connection c = SqlServerFactory.getConnection()) {
            assertTrue(c.isValid(2));
        }
    }
}
