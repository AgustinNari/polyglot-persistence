package com.tpo.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoFactory {
    private static MongoClient mongoClient;
    static {
        String uri = AppConfig.get("mongo.uri");
        ConnectionString connString = new ConnectionString(uri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .build();
        mongoClient = MongoClients.create(settings);
    }
    public static MongoDatabase getDatabase() {
        return mongoClient.getDatabase(AppConfig.get("mongo.db"));
    }
}
