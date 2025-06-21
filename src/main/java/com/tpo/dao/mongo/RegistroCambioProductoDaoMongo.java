package com.tpo.dao.mongo;

import com.tpo.dao.RegistroCambioProductoDao;
import com.tpo.modelo.producto.RegistroCambioProducto;
import com.tpo.config.MongoFactory;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegistroCambioProductoDaoMongo implements RegistroCambioProductoDao {

    private final MongoCollection<Document> coleccion;

    public RegistroCambioProductoDaoMongo() {
        MongoDatabase db = MongoFactory.getDatabase();
        coleccion = db.getCollection("product_history");
    }

    @Override
    public RegistroCambioProducto guardar(RegistroCambioProducto registro) throws Exception {
        Document doc = toDocument(registro);
        coleccion.insertOne(doc);
        ObjectId oid = doc.getObjectId("_id");
        registro.setId(oid.toHexString());
        return registro;
    }

    @Override
    public List<RegistroCambioProducto> listarPorProducto(String productoId) throws Exception {
        List<RegistroCambioProducto> lista = new ArrayList<>();
        for (Document doc : coleccion.find(Filters.eq("productoId", productoId))
                .sort(new Document("fechaCambio", -1))) {
            lista.add(fromDocument(doc));
        }
        return lista;
    }

    @Override
    public List<RegistroCambioProducto> listarTodos() throws Exception {
        List<RegistroCambioProducto> lista = new ArrayList<>();
        for (Document doc : coleccion.find().sort(new Document("fechaCambio", -1))) {
            lista.add(fromDocument(doc));
        }
        return lista;
    }

    private Document toDocument(RegistroCambioProducto r) {
        Document doc = new Document();
        if (r.getId() != null) {
            doc.put("_id", new ObjectId(r.getId()));
        }
        doc.put("productoId", r.getProductoId());
        // fechaCambio: LocalDateTime → Date
        LocalDateTime ldt = r.getFechaCambio();
        if (ldt != null) {
            doc.put("fechaCambio", java.util.Date.from(ldt.atZone(java.time.ZoneId.systemDefault()).toInstant()));
        }
        doc.put("operador", r.getOperador());
        doc.put("tipoOperacion", r.getTipoOperacion());
        // valorAnterior y valorNuevo: Map<String, Object>. Convertir a Document directamente si valores son serializables
        if (r.getValorAnterior() != null) {
            doc.put("valorAnterior", new Document(r.getValorAnterior()));
        }
        if (r.getValorNuevo() != null) {
            doc.put("valorNuevo", new Document(r.getValorNuevo()));
        }
        return doc;
    }

    private RegistroCambioProducto fromDocument(Document doc) {
        RegistroCambioProducto r = new RegistroCambioProducto();
        ObjectId oid = doc.getObjectId("_id");
        r.setId(oid.toHexString());
        r.setProductoId(doc.getString("productoId"));
        java.util.Date fecha = doc.getDate("fechaCambio");
        if (fecha != null) {
            r.setFechaCambio(LocalDateTime.ofInstant(fecha.toInstant(), java.time.ZoneId.systemDefault()));
        }
        r.setOperador(doc.getString("operador"));
        r.setTipoOperacion(doc.getString("tipoOperacion"));
        Document prev = doc.get("valorAnterior", Document.class);
        if (prev != null) {
            r.setValorAnterior(prev);
        }
        Document nuevo = doc.get("valorNuevo", Document.class);
        if (nuevo != null) {
            r.setValorNuevo(nuevo);
        }
        return r;
    }
}
