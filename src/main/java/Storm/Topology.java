package Storm;

import Util.TopChannelSaver;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;

import java.io.IOException;

public class Topology {
    public static void main(String... args) throws IOException {
        // saves the top 10 most viewed live channels to "channels.txt"
        TopChannelSaver.saveTopChannels();

		// Run Apache Storm
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("TwitchSpout", new TwitchSpout());
		builder.setBolt("MessageBolt", new MessageBolt()).shuffleGrouping("TwitchSpout");
		builder.setBolt("EmoteBolt", new EmoteBolt()).shuffleGrouping("MessageBolt");
		builder.setBolt("RabbitBolt", new RabbitBolt()).shuffleGrouping("EmoteBolt");

		Config conf = new Config();
		conf.setDebug(false);

		new LocalCluster().submitTopology("MyFirstTopo", conf, builder.createTopology());

	}
}
