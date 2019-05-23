package Storm;

import Database.MongoConnection;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class EmoteBolt extends BaseBasicBolt {
    private Map<String, String> emoteIDToJSON;

    EmoteBolt() {
        emoteIDToJSON = new HashMap();
        fillInHashMap();
    }

    private void fillInHashMap() {
        System.out.println("Started caching emotes.");
        MongoCollection<Document> emotesCollection = MongoConnection.getDatabase().getCollection("emotes");
        FindIterable<Document> docs = emotesCollection.find();
        for (Document doc : docs)
            emoteIDToJSON.put(doc.get("_id").toString(), doc.toJson());

        System.out.println("Done caching emotes.");
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
        MongoCollection<Document> emotesCollection = MongoConnection.getDatabase().getCollection("emotes");

        Optional<String> rawEmotes = (Optional<String>) tuple.getValueByField("rawEmotes");
        String channelOfMessage = (String) tuple.getValueByField("channelOfMessage");
        if (rawEmotes.isPresent()) {
            String emotesString = rawEmotes.get();
            String[] emotes = emotesString.split("/");
            List<String> emoteIds = new ArrayList<>();
            for (String e : emotes)
                emoteIds.add(e.split(":")[0]);

            for (String emoteId : emoteIds) {
                try {
                    String emoteJson = emoteIDToJSON.get(emoteId);
                    //String emoteJson = getEmoteDetailsFromDB(emoteId);
                    if (emoteJson == null) {
                        System.out.println("Emote is not in the database");
                        continue;
                    }
                    basicOutputCollector.emit(new Values(emoteJson, channelOfMessage));

//					BasicDBObject toBeInserted = BasicDBObject.parse(emoteJson);
//					emotesCollection.insertOne(new Document(toBeInserted.toMap())); // DONT ADD NEW EMOTES, IT'S ENOUGH
//					System.out.println("A new emote inserted");
                } catch (MongoWriteException e) {
                    System.out.println("An emote already exists");
                } catch (JSONException | NullPointerException ignored) {
                }
            }
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("emoteJson", "channelOfMessage"));
    }

    /**
     * Query api.twitchemotes.com API and download the details of emote
     *
     * @param emoteId global unique emote id
     * @return JSON object string that contains emote details
     */
    private static String getEmoteDetailsFromWeb(String emoteId) {
        try {
            JSONArray response = new JSONArray(new Scanner(new URL("https://api.twitchemotes.com/api/v4/emotes?id=" + emoteId).openStream(), StandardCharsets.UTF_8).useDelimiter("\\A").next());
            JSONObject emoteJson = response.getJSONObject(0);
            // Replace id with _id
            emoteJson.put("_id", new ObjectId((String) emoteJson.get("_id")));
            emoteJson.remove("id");
            emoteJson.put("emotion", "TO-BE-COMPLETED");

            return emoteJson.toString();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getEmoteDetailsFromDB(String emoteId) {
        MongoCollection<Document> emotesCollection = MongoConnection.getDatabase().getCollection("emotes");
        Document document = emotesCollection.find(eq("_id", Long.parseLong(emoteId))).first();
        if (document == null)
            return null;

        return document.toJson();
    }
}