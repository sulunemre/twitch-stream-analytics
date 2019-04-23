package Storm;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;

public class Topology {
	public static void main(String... args) {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("TwitchSpout", new TwitchSpout());
		builder.setBolt("MongoBolt", new MongoBolt()).shuffleGrouping("TwitchSpout");

		Config conf = new Config();
		conf.setDebug(true);

		new LocalCluster().submitTopology("MyFirstTopo", conf, builder.createTopology());
	}
}
