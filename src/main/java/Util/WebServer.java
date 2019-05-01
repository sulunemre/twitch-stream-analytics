package Util;

import Storm.EmoteBolt;
import Storm.MessageBolt;
import Storm.TwitchSpout;
import fi.iki.elonen.NanoHTTPD;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;

import java.io.IOException;
import java.util.Map;

public class WebServer extends NanoHTTPD {
	private TopologyBuilder builder;

	public WebServer(int port) throws IOException {
		super(port);
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		System.out.println("\nRunning! Point your browsers to http://localhost:" + port + "/ \n");
		builder = new TopologyBuilder(); // Initialize Apache Storm
	}

	/*
	// Alltaki şeyler main methoda taşınacak
	public static void main(String[] args) {
		try {
			new WebServer();
		} catch (IOException ioe) {
			System.err.println("Couldn't start server:\n" + ioe);
		}
	}
*/
	@Override
	public Response serve(IHTTPSession session) {
		String msg = "<html><body><h1>Hello server</h1>\n";
		Map<String, String> parms = session.getParms();
		if (parms.get("channel") == null) {
			msg += "<form action='?' method='get'>\n  <p>Channel name: <input type='text' name='channel'></p>\n" + "</form>\n";
		} else {
			msg += "<p>Hello, " + parms.get("channel") + "!</p>";
			// Run Apache Storm
			builder.setSpout("TwitchSpout", new TwitchSpout(parms.get("channel")));
			builder.setBolt("MessageBolt", new MessageBolt()).shuffleGrouping("TwitchSpout");
			builder.setBolt("EmoteBolt", new EmoteBolt()).shuffleGrouping("MessageBolt");

			Config conf = new Config();
			conf.setDebug(false);

			new LocalCluster().submitTopology("MyFirstTopo", conf, builder.createTopology());
		}
		return newFixedLengthResponse(msg + "</body></html>\n");
	}
}