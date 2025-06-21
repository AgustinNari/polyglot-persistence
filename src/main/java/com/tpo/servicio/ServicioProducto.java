package com.tpo.servicio;

import com.tpo.dao.ProductoDao;
import com.tpo.dao.RegistroCambioProductoDao;
import com.tpo.modelo.producto.Producto;
import com.tpo.modelo.producto.RegistroCambioProducto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ServicioProducto {

    private final ProductoDao productoDao;
    private final RegistroCambioProductoDao registroDao;

    public ServicioProducto(ProductoDao productoDao, RegistroCambioProductoDao registroDao) {
        this.productoDao = productoDao;
        this.registroDao = registroDao;
    }

    public Producto crearProducto(Producto p, String operador) throws Exception {
        // Inicializar fechas
        p.setFechaCreacion(LocalDateTime.now());
        p.setFechaActualizacion(LocalDateTime.now());
        Producto guardado = productoDao.guardar(p);
        // Registrar auditoría: valorAnterior null, valorNuevo con todos los campos
        Map<String,Object> valorAnterior = null;
        Map<String,Object> valorNuevo = toMap(guardado);
        RegistroCambioProducto reg = new RegistroCambioProducto(
                guardado.getId(), LocalDateTime.now(), operador, valorAnterior, valorNuevo, "CREAR");
        registroDao.guardar(reg);
        return guardado;
    }

    public Producto actualizarProducto(Producto p, String operador) throws Exception {
        Optional<Producto> antesOpt = productoDao.buscarPorId(p.getId());
        if (antesOpt.isEmpty()) {
            throw new IllegalArgumentException("Producto no existe con id: " + p.getId());
        }
        Producto antes = antesOpt.get();
        Map<String,Object> valorAnterior = toMap(antes);
        // Actualizar campos y fecha
        p.setFechaCreacion(antes.getFechaCreacion());
        p.setFechaActualizacion(LocalDateTime.now());
        productoDao.actualizar(p);
        Map<String,Object> valorNuevo = toMap(p);
        RegistroCambioProducto reg = new RegistroCambioProducto(
                p.getId(), LocalDateTime.now(), operador, valorAnterior, valorNuevo, "MODIFICAR");
        registroDao.guardar(reg);
        return p;
    }

    public void eliminarProducto(String id, String operador) throws Exception {
        Optional<Producto> antesOpt = productoDao.buscarPorId(id);
        if (antesOpt.isEmpty()) {
            throw new IllegalArgumentException("Producto no existe con id: " + id);
        }
        Producto antes = antesOpt.get();
        Map<String,Object> valorAnterior = toMap(antes);
        productoDao.eliminarPorId(id);
        RegistroCambioProducto reg = new RegistroCambioProducto(
                id, LocalDateTime.now(), operador, valorAnterior, null, "ELIMINAR");
        registroDao.guardar(reg);
    }

    public Optional<Producto> buscarProducto(String id) throws Exception {
        return productoDao.buscarPorId(id);
    }

    public List<Producto> listarProductos() throws Exception {
        return productoDao.listarTodos();
    }


    private Map<String,Object> toMap(Producto p) {
        Map<String,Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("nombre", p.getNombre());
        map.put("descripcion", p.getDescripcion());
        map.put("precio", p.getPrecio());
        map.put("urlsFotos", p.getUrlsFotos());
        map.put("urlsVideos", p.getUrlsVideos());
        map.put("comentarios", p.getComentarios());
        map.put("etiquetas", p.getEtiquetas());
        map.put("fechaCreacion", p.getFechaCreacion());
        map.put("fechaActualizacion", p.getFechaActualizacion());
        return map;
    }
}
