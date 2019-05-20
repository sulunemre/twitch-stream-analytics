package Storm;

import Database.MongoConnection;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class EmoteBolt extends BaseBasicBolt {
	@Override
	public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
		MongoCollection<Document> emotesCollection = MongoConnection.getDatabase().getCollection("emotes");

		Optional<String> rawEmotes = (Optional<String>) tuple.getValueByField("rawEmotes");
		if (rawEmotes.isPresent()) {
			String emotesString = rawEmotes.get();
			String[] emotes = emotesString.split("/");
			List<String> emoteIds = new ArrayList<>();
			for (String e : emotes)
				emoteIds.add(e.split(":")[0]);

			for (String emoteId : emoteIds) {
				String emoteJson = getEmoteDetails(emoteId);
				basicOutputCollector.emit(new Values(emoteJson));
				BasicDBObject toBeInserted = BasicDBObject.parse(emoteJson);
				try {
					emotesCollection.insertOne(new Document(toBeInserted.toMap()));
					System.out.println("A new emote inserted");
				} catch (MongoWriteException e) {
					System.out.println("An emote already exists");
				}
			}
		}

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
		outputFieldsDeclarer.declare(new Fields("emoteJson"));
	}

	/**
	 * Query api.twitchemotes.com API and download the details of emote
	 *
	 * @param emoteId global unique emote id
	 * @return JSON object string that contains emote details
	 */
	private static String getEmoteDetails(String emoteId) {
		try {
			JSONArray response = new JSONArray(new Scanner(new URL("https://api.twitchemotes.com/api/v4/emotes?id=" + emoteId).openStream(), StandardCharsets.UTF_8).useDelimiter("\\A").next());
			JSONObject emoteJson = response.getJSONObject(0);
			// Replace id with _id
			emoteJson.put("_id", emoteJson.get("id"));
			emoteJson.remove("id");
			emoteJson.put("emotion", "TO-BE-COMPLETED");

			return emoteJson.toString();
		} catch (IOException | JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
