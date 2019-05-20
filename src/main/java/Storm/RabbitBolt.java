package Storm;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitBolt extends BaseBasicBolt {
	private static Channel channel;
	private static final String QUEUE_NAME = "hello";

	static {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		try {
			Connection connection = factory.newConnection();
			channel = connection.createChannel();
			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
		String emoteJson = (String) tuple.getValueByField("emoteJson");
		try {
			channel.basicPublish("", QUEUE_NAME, null, emoteJson.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

	}
}
