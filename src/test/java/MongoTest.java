//import Database.MongoConnection;
//import com.mongodb.client.MongoCollection;
//import org.bson.Document;
//
//import static com.mongodb.client.model.Filters.eq;
//
//public class MongoTest {
//	public static void main(String... args) {
//
//		System.out.println(getEmoteDetailsFromDB("69"));
//
//	}
//
//	private static String getEmoteDetailsFromDB(String emoteId) {
//		MongoCollection<Document> emotesCollection = MongoConnection.getDatabase().getCollection("emotes");
//		Document document = emotesCollection.find(eq("_id", Long.parseLong(emoteId))).first();
//		if (document == null)
//			return null;
//
//		return document.toString();
//	}
//}
