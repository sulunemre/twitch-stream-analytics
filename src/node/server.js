var amqp = require('amqplib/callback_api');
var groupBy = require('json-groupby');
var app = require('http').createServer(handler),
	io = require('socket.io').listen(app),
	fs = require('fs');

var refreshTimeInSeconds = 5;
var slidingWindowIntervalInMinutes = 0.5;
var port = 8081;
var messageQueueIP = 'amqp://139.179.55.138';

var channelsJSONgroupedByEmotions;
var arrayOfChannelJSONs;
var messages = [];

/**
 * Remove messages that was sent more than x minutes ago.
 * @param minutes
 */
function filterMessagesByMinutes(minutes) {
	messages = messages.filter(function (message) {
		return (Date.now() - parseInt(message.timestamp)) / (60 * 1000) < minutes;
	});
}

/**
 * Group and count the number of messages belonging to each emotion
 * and assign this to emotionsJSON variable.
 */
function groupMessagesByEmotions() {
	channelsJSONgroupedByEmotions = groupBy(messages, ['channelOfMessage'], ['emotion'])
	arrayOfChannelJSONs = []
	console.log("************")
	for (var channel in channelsJSONgroupedByEmotions) {
		var JSONofCurrentChannel = {"channelName": channel, "dataPoints": []};
		channelsJSONgroupedByEmotions[channel]["emotion"].sort();
		var JSONofCurrentEmotion = {}
		for (let i = 0; i < channelsJSONgroupedByEmotions[channel]["emotion"].length; i++) {

			//currentEmotion will be one of "disgust", "amused", "love" ...
			let currentEmotion = channelsJSONgroupedByEmotions[channel]["emotion"][i];

			//We don't need to keep emotions named "TO-BE-COMPLETED"
			if (currentEmotion === "TO-BE-COMPLETED")
				continue;

			JSONofCurrentEmotion.emotion = currentEmotion;

			//if we see this emotion first time in this chat
			//give it number 1 else increment it.
			if (JSONofCurrentEmotion.y == undefined) {
				JSONofCurrentEmotion.y = 1;
			} else {
				JSONofCurrentEmotion.y += 1;
			}

			// If this is the last element of a channel's array of emotions or the next
			// emotion is different than this emotion, put current emotion into dataPoint.
			if (i + 1 == channelsJSONgroupedByEmotions[channel]["emotion"].length ||
				channelsJSONgroupedByEmotions[channel]["emotion"][i] != channelsJSONgroupedByEmotions[channel]["emotion"][i + 1]) {
				JSONofCurrentChannel.dataPoints.push(JSONofCurrentEmotion);
				JSONofCurrentEmotion = {};
			}

		}
		//after traversing the list of emotions and counting,
		//we don't need them as an array inside our JSON.
		delete channelsJSONgroupedByEmotions[channel]["emotion"];
		arrayOfChannelJSONs.push(JSONofCurrentChannel);
	}
	console.log(channelsJSONgroupedByEmotions)
}

amqp.connect(messageQueueIP, function (error0, connection) {
	setInterval(function () {
		filterMessagesByMinutes(slidingWindowIntervalInMinutes);
		groupMessagesByEmotions();
	}, refreshTimeInSeconds * 1000) // filter messages every x seconds

	connection.createChannel(function (error1, channel) {
		var queueName = 'hello';

		channel.assertQueue(queueName, {
			durable: false
		});

		console.log(" [*] Waiting for messages in %s. To exit press CTRL+C", queueName);

		channel.consume(queueName, function (msg) {
			var message = JSON.parse(msg.content.toString());
			messages.push(message);
		}, {
			noAck: true
		});
	});
});

// Start listening to the clients on port.
app.listen(port);

function handler(req, res) {
	fs.readFile(__dirname + '/index.html',
		function (err, data) {
			if (err) {
				res.writeHead(500);
				return res.end('Error loading index.html');
			}
			res.writeHead(200);
			res.end(data);
		});
}

// Manage connections
io.sockets.on('connection', function (socket) {
	var periodInMilliseconds = refreshTimeInSeconds * 1000;
	setInterval(sendJsonToClient, periodInMilliseconds);

	function sendJsonToClient() {
		socket.emit('server request', arrayOfChannelJSONs);
	}
});