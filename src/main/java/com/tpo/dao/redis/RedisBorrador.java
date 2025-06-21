package com.tpo.dao.redis;

import com.tpo.config.RedisFactory;
import redis.clients.jedis.Jedis;

public class RedisBorrador {
    public static void vaciarRedis() {
        try (Jedis jedis = RedisFactory.getConnection()) {
            jedis.flushDB(); // o jedis.flushAll();
            System.out.println("Redis vaciado.");
        }
    }
}
