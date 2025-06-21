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
            // Inicialmente vacío: asegurarse de que no exista
            jedis.del(key);
            // Limpiar pilas undo/redo
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
            // Registrar snapshot antes de mutar
            registrarSnapshot(jedis, usuarioId);
            // Serializar LineaCarrito a JSON
            String jsonLinea = gson.toJson(linea);
            jedis.hset(carritoKey, linea.getProductoId(), jsonLinea);
            // Limpiar redo al mutar
            jedis.del(PREFIX_REDO + usuarioId);
        }
    }

    @Override
    public void actualizarLinea(String usuarioId, LineaCarrito linea) throws Exception {
        try (Jedis jedis = RedisFactory.getConnection()) {
            String carritoKey = PREFIX_CARRITO + usuarioId;
            // Registrar snapshot antes de mutar
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
        // Este método puede usarse para guardar un log textual de la acción:
        // Puede almacenarse en lista: "accion:AGREGAR linea:{...}"
        try (Jedis jedis = RedisFactory.getConnection()) {
            String keyAcciones = "carrito:acciones:" + usuarioId;
            String entry = accion + ":" + gson.toJson(linea);
            jedis.lpush(keyAcciones, entry);
            // Podrías limitar tamaño de la lista: jedis.ltrim(keyAcciones, 0, 99) para max 100 registros.
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
            // Obtener estado actual antes de deshacer
            List<LineaCarrito> estadoActual = obtenerLineas(usuarioId);
            String estadoActualJson = gson.toJson(estadoActual);
            // Pop último snapshot para restaurar
            String snapJson = jedis.lpop(undoKey);
            if (snapJson == null) {
                throw new IllegalStateException("No hay nada para deshacer");
            }
            // Guardar estado actual en redo stack
            jedis.lpush(redoKey, estadoActualJson);
            // Restaurar snapshot: parsear lista de LineaCarrito
            Type tipoLista = new TypeToken<List<LineaCarrito>>(){}.getType();
            List<LineaCarrito> snapshot = gson.fromJson(snapJson, tipoLista);
            // Limpiar carrito actual
            jedis.del(carritoKey);
            // Restaurar cada línea
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
            // Obtener estado actual antes de rehacer
            List<LineaCarrito> estadoActual = obtenerLineas(usuarioId);
            String estadoActualJson = gson.toJson(estadoActual);
            // Pop último redo snapshot
            String snapJson = jedis.lpop(redoKey);
            if (snapJson == null) {
                throw new IllegalStateException("No hay nada para rehacer");
            }
            // Guardar estado actual en undo stack
            jedis.lpush(undoKey, estadoActualJson);
            // Restaurar snapshot de redo
            Type tipoLista = new TypeToken<List<LineaCarrito>>(){}.getType();
            List<LineaCarrito> snapshot = gson.fromJson(snapJson, tipoLista);
            // Limpiar carrito actual
            jedis.del(carritoKey);
            // Restaurar cada línea
            for (LineaCarrito linea : snapshot) {
                jedis.hset(carritoKey, linea.getProductoId(), gson.toJson(linea));
            }
        }
    }

    // Método auxiliar: antes de mutar, guardar snapshot de estado actual en undo stack
    private void registrarSnapshot(Jedis jedis, String usuarioId) throws Exception {
        String carritoKey = PREFIX_CARRITO + usuarioId;
        String undoKey = PREFIX_UNDO + usuarioId;
        // Obtener estado actual
        Map<String, String> all = jedis.hgetAll(carritoKey);
        List<LineaCarrito> estadoActual = new ArrayList<>();
        Type tipo = new TypeToken<LineaCarrito>(){}.getType();
        for (String json : all.values()) {
            estadoActual.add(gson.fromJson(json, tipo));
        }
        String estadoJson = gson.toJson(estadoActual);
        // Push en undo stack
        jedis.lpush(undoKey, estadoJson);
        // Opcionalmente limitar tamaño de undo stack: jedis.ltrim(undoKey, 0, 49) para max 50 snapshots.
    }
}
