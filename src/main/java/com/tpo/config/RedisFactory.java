package com.tpo.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisFactory {
    private static final JedisPool pool;

    static {
        try {
            String host = AppConfig.get("redis.host");
            int port = Integer.parseInt(AppConfig.get("redis.port"));
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(10);
            config.setMaxIdle(5);
            config.setMinIdle(1);
            pool = new JedisPool(config, host, port);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("Error inicializando RedisFactory: " + e.getMessage());
        }
    }

    public static Jedis getConnection() {
        return pool.getResource();
    }
}
