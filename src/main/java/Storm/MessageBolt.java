package Storm;

import Database.MongoConnection;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.mongodb.client.MongoCollection;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageBolt extends BaseBasicBolt {
	@Override
	public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
		MongoCollection<Document> messagesCollection = MongoConnection.getDatabase().getCollection("messages");

		IRCMessageEvent rawEvent = (IRCMessageEvent) tuple.getValueByField("rawEvent");
		String channelOfMessage = null;
		try {
			channelOfMessage = rawEvent.getChannel().getName();
		} catch (Exception ignored) {
		}
		if (rawEvent.getMessage().isPresent()) {
			Document document = new Document()
					.append("channelName", channelOfMessage)
					.append("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()))
					.append("messageBody", rawEvent.getMessage().get());

			messagesCollection.insertOne(document);
		}


		basicOutputCollector.emit(new Values(rawEvent.getTagValue("emotes"), channelOfMessage));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
		outputFieldsDeclarer.declare(new Fields("rawEmotes", "channelOfMessage"));
	}
}
