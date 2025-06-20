package com.tpo.config;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RedisTest {
    @Test
    void testConnection() {
        try (Jedis jedis = RedisFactory.getConnection()) {
            assertEquals("PONG", jedis.ping());
        }
    }
}
