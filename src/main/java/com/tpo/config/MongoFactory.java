package com.tpo.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoFactory {
    private static final MongoClient cliente;
    private static final String DB_NAME;

    static {
        try {
            String uri = AppConfig.get("mongo.uri");
            DB_NAME = AppConfig.get("mongo.db");
            cliente = MongoClients.create(uri);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("Error inicializando MongoFactory: " + e.getMessage());
        }
    }

    public static MongoDatabase getDatabase() {
        return cliente.getDatabase(DB_NAME);
    }

    private static void vaciarMongo() {
        var db = MongoFactory.getDatabase();
        db.getCollection("products").deleteMany(new org.bson.Document());
        db.getCollection("product_history").deleteMany(new org.bson.Document());
        System.out.println("Mongo: colecciones vaciadas");
    }

    public static MongoClient getClient() {
        return cliente;
    }

    public static String getDatabaseName() {
        return DB_NAME;
    }

}
