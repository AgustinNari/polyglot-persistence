package com.tpo.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisFactory {
    private static JedisPool pool;
    static {
        JedisPoolConfig cfg = new JedisPoolConfig();
        cfg.setMaxTotal(20);
        String host = AppConfig.get("redis.host");
        int port = Integer.parseInt(AppConfig.get("redis.port"));
        pool = new JedisPool(cfg, host, port);
    }
    public static Jedis getConnection() {
        return pool.getResource();
    }
}
