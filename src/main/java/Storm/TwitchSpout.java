package Storm;

import Twitch.TwitchConnection;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class TwitchSpout extends BaseRichSpout {
	private SpoutOutputCollector spoutOutputCollector;
	private Queue<TwitchMessage> incomingMessages;

	@Override
	public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
		System.out.println("I AM A NEW TWITCH SPOUT");

		this.spoutOutputCollector = spoutOutputCollector;
		incomingMessages = new LinkedList<>();

		TwitchClient twitchClient = TwitchConnection.getTwitchClient();
		joinChannels(twitchClient);
		twitchClient.getEventManager().onEvent(IRCMessageEvent.class).subscribe(event -> {
			if (event.getMessage().isPresent()) {
				TwitchMessage newMessage = new TwitchMessage(
						event.getChannel().getName(),
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()),
						event.getMessage().get()
				);
				incomingMessages.add(newMessage);
			}
		});
	}

	@Override
	public void nextTuple() {
		if (!incomingMessages.isEmpty()) {
			TwitchMessage message = incomingMessages.poll();
			spoutOutputCollector.emit(new Values(message.channelName, message.date, message.messageBody));
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
		outputFieldsDeclarer.declare(new Fields("channelName", "date", "messageBody"));
	}

	/**
	 * Join all channels in the channels.txt file
	 */
	private void joinChannels(TwitchClient twitchClient) {
		try (BufferedReader br = new BufferedReader(new FileReader("channels.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				twitchClient.getChat().joinChannel(line);
				System.out.println("Joined to " + line + " channel!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class TwitchMessage {
	TwitchMessage(String channelName, String date, String messageBody) {
		this.channelName = channelName;
		this.date = date;
		this.messageBody = messageBody;
	}

	String channelName;
	String date;
	String messageBody;
}
