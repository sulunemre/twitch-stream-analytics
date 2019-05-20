package Util;

import Database.MongoConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * push official emoticons to "emoticonsOfficial" collection
 */
public class EmoteFilter {
    public static void main(String... args) {
        MongoDatabase connection = MongoConnection.getDatabase();
        MongoCollection<Document> collection = connection.getCollection("emotes");
        System.out.println(collection.countDocuments());

        List<Document> documents = collection.find(eq("emoticon_set", 0)).into(new ArrayList<>());

        MongoCollection<Document> emote_collection = connection.getCollection("emotesOfficial");
        for (Document document : documents) {
            emote_collection.insertOne(document);
        }

        System.out.println(emote_collection.countDocuments());
    }
}
