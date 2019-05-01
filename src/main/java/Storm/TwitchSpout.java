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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class TwitchSpout extends BaseRichSpout {
	private SpoutOutputCollector spoutOutputCollector;
	private Queue<IRCMessageEvent> incomingEvents;
	private String channelName;
	//private ArrayList<String> channels; // to be used with multiple channels

	public TwitchSpout(String channelName) {
		super();
		this.channelName = channelName;
	}
	@Override
	public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
		System.out.println("I AM A NEW TWITCH SPOUT");

		this.spoutOutputCollector = spoutOutputCollector;
		incomingEvents = new LinkedList<>();

		TwitchClient twitchClient = TwitchConnection.getTwitchClient();
		joinChannels(twitchClient);
		twitchClient.getEventManager().onEvent(IRCMessageEvent.class).subscribe(event -> incomingEvents.add(event));
	}

	@Override
	public void nextTuple() {
		if (!incomingEvents.isEmpty()) {
			IRCMessageEvent event = incomingEvents.poll();
			spoutOutputCollector.emit(new Values(event));
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
		outputFieldsDeclarer.declare(new Fields("rawEvent"));
	}

	/**
	 * Join all channels in the channels.txt file
	 */
	private void joinChannels(TwitchClient twitchClient) {
		try (BufferedReader br = new BufferedReader(new FileReader("channels.txt"))) {
			twitchClient.getChat().joinChannel(channelName);
			System.out.println("Joined to " + channelName + " channel!");
			/*
			String line;
			while ((line = br.readLine()) != null) {
				twitchClient.getChat().joinChannel(line);
				System.out.println("Joined to " + line + " channel!");
			}
			 */
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}