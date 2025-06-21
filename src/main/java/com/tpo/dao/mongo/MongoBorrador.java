package com.tpo.dao.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.tpo.config.AppConfig;

public class MongoBorrador {
    public static void vaciarColecciones() {
        String uri = AppConfig.get("mongo.uri"); // si usas AppConfig
        try (MongoClient client = MongoClients.create(uri)) {
            MongoDatabase db = client.getDatabase(AppConfig.get("mongo.db"));
            db.getCollection("products").deleteMany(new org.bson.Document());
            db.getCollection("product_history").deleteMany(new org.bson.Document());
            System.out.println("Colecciones Mongo vaciadas.");
        }
    }
}
