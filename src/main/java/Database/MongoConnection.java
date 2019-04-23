package Database;

import Util.Secrets;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {
	private static MongoClientURI uri = new MongoClientURI(Secrets.MONGO_STRING);
	private static MongoClient mongoClient = new MongoClient(uri);
	private static MongoDatabase database = mongoClient.getDatabase("test");

	public static MongoDatabase getDatabase() {
		return database;
	}

	private MongoConnection() {
	}
}
