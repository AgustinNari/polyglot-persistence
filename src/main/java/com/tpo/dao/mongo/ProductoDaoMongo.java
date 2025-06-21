package com.tpo.dao.mongo;

import com.tpo.dao.ProductoDao;
import com.tpo.modelo.producto.Producto;
import com.tpo.config.MongoFactory;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoDaoMongo implements ProductoDao {

    private final MongoCollection<Document> coleccion;

    public ProductoDaoMongo() {
        MongoDatabase db = MongoFactory.getDatabase();
        coleccion = db.getCollection("products");
    }

    @Override
    public Producto guardar(Producto producto) throws Exception {
        Document doc = toDocument(producto);
        coleccion.insertOne(doc);
        ObjectId id = doc.getObjectId("_id");
        producto.setId(id.toHexString());
        return producto;
    }

    @Override
    public void actualizar(Producto producto) throws Exception {
        if (producto.getId() == null) {
            throw new IllegalArgumentException("El id de Producto es nulo al actualizar");
        }
        ObjectId oid = new ObjectId(producto.getId());
        Document doc = toDocument(producto);
        // Asegurarse de no sobrescribir _id
        doc.remove("_id");
        coleccion.replaceOne(Filters.eq("_id", oid), doc);
    }

    @Override
    public void eliminarPorId(String id) throws Exception {
        ObjectId oid = new ObjectId(id);
        coleccion.deleteOne(Filters.eq("_id", oid));
    }

    @Override
    public Optional<Producto> buscarPorId(String id) throws Exception {
        ObjectId oid = new ObjectId(id);
        Document doc = coleccion.find(Filters.eq("_id", oid)).first();
        if (doc == null) return Optional.empty();
        Producto p = fromDocument(doc);
        return Optional.of(p);
    }

    @Override
    public List<Producto> buscarPorNombre(String nombre) throws Exception {
        List<Producto> lista = new ArrayList<>();
        for (Document doc : coleccion.find(Filters.regex("nombre", nombre, "i"))) {
            lista.add(fromDocument(doc));
        }
        return lista;
    }

    @Override
    public List<Producto> listarTodos() throws Exception {
        List<Producto> lista = new ArrayList<>();
        for (Document doc : coleccion.find()) {
            lista.add(fromDocument(doc));
        }
        return lista;
    }

    // Convierte POJO a Document
    private Document toDocument(Producto p) {
        Document doc = new Document();
        if (p.getId() != null) {
            doc.put("_id", new ObjectId(p.getId()));
        }
        doc.put("nombre", p.getNombre());
        doc.put("descripcion", p.getDescripcion());
        // BigDecimal no mapea directo: convertir a String o Double. Mejor String para exactitud.
        doc.put("precio", p.getPrecio().toString());
        doc.put("urlsFotos", p.getUrlsFotos());
        doc.put("urlsVideos", p.getUrlsVideos());
        doc.put("comentarios", p.getComentarios());
        doc.put("etiquetas", p.getEtiquetas());
        // Fechas: LocalDateTime → String o Date. MongoDocument Date toma java.util.Date:
        if (p.getFechaCreacion() != null) {
            doc.put("fechaCreacion", java.util.Date.from(p.getFechaCreacion().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        }
        if (p.getFechaActualizacion() != null) {
            doc.put("fechaActualizacion", java.util.Date.from(p.getFechaActualizacion().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        }
        return doc;
    }

    // Convierte Document a POJO
    private Producto fromDocument(Document doc) {
        Producto p = new Producto();
        ObjectId oid = doc.getObjectId("_id");
        p.setId(oid.toHexString());
        p.setNombre(doc.getString("nombre"));
        p.setDescripcion(doc.getString("descripcion"));
        // precio: recuperado como String o Double. Si guardamos como String, parsear:
        String precioStr = doc.getString("precio");
        if (precioStr != null) {
            p.setPrecio(new BigDecimal(precioStr));
        } else if (doc.get("precio") instanceof Number) {
            p.setPrecio(new BigDecimal(((Number) doc.get("precio")).toString()));
        }
        // listas
        List<String> fotos = doc.getList("urlsFotos", String.class);
        p.setUrlsFotos(fotos);
        List<String> videos = doc.getList("urlsVideos", String.class);
        p.setUrlsVideos(videos);
        List<String> comentarios = doc.getList("comentarios", String.class);
        p.setComentarios(comentarios);
        List<String> etiquetas = doc.getList("etiquetas", String.class);
        p.setEtiquetas(etiquetas);
        // fechas
        java.util.Date fechaC = doc.getDate("fechaCreacion");
        if (fechaC != null) {
            p.setFechaCreacion(LocalDateTime.ofInstant(fechaC.toInstant(), java.time.ZoneId.systemDefault()));
        }
        java.util.Date fechaA = doc.getDate("fechaActualizacion");
        if (fechaA != null) {
            p.setFechaActualizacion(LocalDateTime.ofInstant(fechaA.toInstant(), java.time.ZoneId.systemDefault()));
        }
        return p;
    }
}
