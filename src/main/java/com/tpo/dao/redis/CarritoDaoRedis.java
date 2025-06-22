package com.tpo.dao.redis;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tpo.dao.CarritoDao;
import com.tpo.modelo.pedido.LineaCarrito;
import com.tpo.config.RedisFactory;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CarritoDaoRedis implements CarritoDao {

    private static final String PREFIX_CARRITO = "carrito:";
    private static final String PREFIX_UNDO = "carrito:undo:";
    private static final String PREFIX_REDO = "carrito:redo:";
    private final Gson gson = new Gson();

    @Override
    public void crearCarrito(String usuarioId) throws Exception {
        try (Jedis jedis = RedisFactory.getConnection()) {
            String key = PREFIX_CARRITO + usuarioId;

            jedis.del(key);

            jedis.del(PREFIX_UNDO + usuarioId);
            jedis.del(PREFIX_REDO + usuarioId);
        }
    }

    @Override
    public void eliminarCarrito(String usuarioId) throws Exception {
        try (Jedis jedis = RedisFactory.getConnection()) {
            jedis.del(PREFIX_CARRITO + usuarioId, PREFIX_UNDO + usuarioId, PREFIX_REDO + usuarioId);
        }
    }

    @Override
    public void agregarLinea(String usuarioId, LineaCarrito linea) throws Exception {
        try (Jedis jedis = RedisFactory.getConnection()) {
            String carritoKey = PREFIX_CARRITO + usuarioId;

            registrarSnapshot(jedis, usuarioId);

            String jsonLinea = gson.toJson(linea);
            jedis.hset(carritoKey, linea.getProductoId(), jsonLinea);

            jedis.del(PREFIX_REDO + usuarioId);
        }
    }

    @Override
    public void actualizarLinea(String usuarioId, LineaCarrito linea) throws Exception {
        try (Jedis jedis = RedisFactory.getConnection()) {
            String carritoKey = PREFIX_CARRITO + usuarioId;

            registrarSnapshot(jedis, usuarioId);
            String jsonLinea = gson.toJson(linea);
            jedis.hset(carritoKey, linea.getProductoId(), jsonLinea);
            jedis.del(PREFIX_REDO + usuarioId);
        }
    }

    @Override
    public void eliminarLinea(String usuarioId, String productoId) throws Exception {
        try (Jedis jedis = RedisFactory.getConnection()) {
            String carritoKey = PREFIX_CARRITO + usuarioId;
            registrarSnapshot(jedis, usuarioId);
            jedis.hdel(carritoKey, productoId);
            jedis.del(PREFIX_REDO + usuarioId);
        }
    }

    @Override
    public List<LineaCarrito> obtenerLineas(String usuarioId) throws Exception {
        try (Jedis jedis = RedisFactory.getConnection()) {
            String carritoKey = PREFIX_CARRITO + usuarioId;
            Map<String, String> all = jedis.hgetAll(carritoKey);
            List<LineaCarrito> lista = new ArrayList<>();
            Type tipo = new TypeToken<LineaCarrito>(){}.getType();
            for (String json : all.values()) {
                LineaCarrito linea = gson.fromJson(json, tipo);
                lista.add(linea);
            }
            return lista;
        }
    }

    @Override
    public void limpiarCarrito(String usuarioId) throws Exception {
        try (Jedis jedis = RedisFactory.getConnection()) {
            String carritoKey = PREFIX_CARRITO + usuarioId;
            registrarSnapshot(jedis, usuarioId);
            jedis.del(carritoKey);
            jedis.del(PREFIX_REDO + usuarioId);
        }
    }

    @Override
    public void registrarAccion(String usuarioId, String accion, LineaCarrito linea) throws Exception {

        try (Jedis jedis = RedisFactory.getConnection()) {
            String keyAcciones = "carrito:acciones:" + usuarioId;
            String entry = accion + ":" + gson.toJson(linea);
            jedis.lpush(keyAcciones, entry);

        }
    }

    @Override
    public boolean puedeDeshacer(String usuarioId) throws Exception {
        try (Jedis jedis = RedisFactory.getConnection()) {
            String undoKey = PREFIX_UNDO + usuarioId;
            return jedis.llen(undoKey) > 0;
        }
    }

    @Override
    public void deshacer(String usuarioId) throws Exception {
        try (Jedis jedis = RedisFactory.getConnection()) {
            String undoKey = PREFIX_UNDO + usuarioId;
            String redoKey = PREFIX_REDO + usuarioId;
            String carritoKey = PREFIX_CARRITO + usuarioId;

            List<LineaCarrito> estadoActual = obtenerLineas(usuarioId);
            String estadoActualJson = gson.toJson(estadoActual);

            String snapJson = jedis.lpop(undoKey);
            if (snapJson == null) {
                throw new IllegalStateException("No hay nada para deshacer");
            }

            jedis.lpush(redoKey, estadoActualJson);

            Type tipoLista = new TypeToken<List<LineaCarrito>>(){}.getType();
            List<LineaCarrito> snapshot = gson.fromJson(snapJson, tipoLista);

            jedis.del(carritoKey);

            for (LineaCarrito linea : snapshot) {
                jedis.hset(carritoKey, linea.getProductoId(), gson.toJson(linea));
            }
        }
    }

    @Override
    public boolean puedeRehacer(String usuarioId) throws Exception {
        try (Jedis jedis = RedisFactory.getConnection()) {
            String redoKey = PREFIX_REDO + usuarioId;
            return jedis.llen(redoKey) > 0;
        }
    }

    @Override
    public void rehacer(String usuarioId) throws Exception {
        try (Jedis jedis = RedisFactory.getConnection()) {
            String undoKey = PREFIX_UNDO + usuarioId;
            String redoKey = PREFIX_REDO + usuarioId;
            String carritoKey = PREFIX_CARRITO + usuarioId;

            List<LineaCarrito> estadoActual = obtenerLineas(usuarioId);
            String estadoActualJson = gson.toJson(estadoActual);

            String snapJson = jedis.lpop(redoKey);
            if (snapJson == null) {
                throw new IllegalStateException("No hay nada para rehacer");
            }

            jedis.lpush(undoKey, estadoActualJson);

            Type tipoLista = new TypeToken<List<LineaCarrito>>(){}.getType();
            List<LineaCarrito> snapshot = gson.fromJson(snapJson, tipoLista);

            jedis.del(carritoKey);

            for (LineaCarrito linea : snapshot) {
                jedis.hset(carritoKey, linea.getProductoId(), gson.toJson(linea));
            }
        }
    }


    private void registrarSnapshot(Jedis jedis, String usuarioId) throws Exception {
        String carritoKey = PREFIX_CARRITO + usuarioId;
        String undoKey = PREFIX_UNDO + usuarioId;

        Map<String, String> all = jedis.hgetAll(carritoKey);
        List<LineaCarrito> estadoActual = new ArrayList<>();
        Type tipo = new TypeToken<LineaCarrito>(){}.getType();
        for (String json : all.values()) {
            estadoActual.add(gson.fromJson(json, tipo));
        }
        String estadoJson = gson.toJson(estadoActual);

        jedis.lpush(undoKey, estadoJson);

    }
}
