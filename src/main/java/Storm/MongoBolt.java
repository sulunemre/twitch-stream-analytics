package Storm;

import com.mongodb.client.MongoDatabase;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;
import org.bson.Document;
import Database.MongoConnection;

public class MongoBolt extends BaseBasicBolt {
	@Override
	public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
		MongoDatabase db = MongoConnection.getDatabase();
		String channelName = tuple.getStringByField("channelName");
		String date = tuple.getStringByField("date");
		String messageBody = tuple.getStringByField("messageBody");
		if (channelName != null) {
			Document document = new Document()
					.append("channelName", channelName)
					.append("date", date)
					.append("messageBody", messageBody);

			db.getCollection("messages").insertOne(document);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

	}
}
