package com.tpo.config;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MongoTest {
    @Test
    void testConnection() {
        MongoDatabase db = MongoFactory.getDatabase();
        Document result = db.runCommand(new Document("ping", 1));
        assertEquals(1.0, result.getDouble("ok"));
    }
}
