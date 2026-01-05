/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoConnection {

    private static final String URI = "mongodb://localhost:27017";
    private static final String DB_NAME = "users_manager";

    private static final MongoClient client = MongoClients.create(URI);
    private static final MongoDatabase database = client.getDatabase(DB_NAME);

    public static MongoCollection<Document> getUsersCollection() {
        return database.getCollection("users");
    }
}
