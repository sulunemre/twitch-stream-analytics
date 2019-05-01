package Storm;

import Util.WebServer;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;

import java.io.IOException;

public class Topology {
	public static void main(String... args) {
		// Run web server
		try {
			new WebServer(8080);
		} catch (IOException e) {
			System.out.println("Could not run web server");
		}

	}
}
