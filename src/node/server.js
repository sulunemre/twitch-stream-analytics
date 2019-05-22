var amqp = require('amqplib/callback_api');
var groupBy = require('json-groupby');
var app = require('http').createServer(handler),
	io = require('socket.io').listen(app),
	fs = require('fs');

var refreshTimeInSeconds = 5;
var slidingWindowIntervalInMinutes = 0.5;
var port = 8081;
var messageQueueIP = 'amqp://10.76.225.59';

var channelsJSON;
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
	channelsJSON = groupBy(messages, ['channelOfMessage'], ['emotion'])
	console.log("************")
	for (var channel in channelsJSON) {
		for (let i = 0; i < channelsJSON[channel]["emotion"].length; i++) {

			//currentEmotion will be one of "disgust", "amused", "love" ...
			let currentEmotion = channelsJSON[channel]["emotion"][i];

			//We don't need to keep emotions named "TO-BE-COMPLETED"
			if (currentEmotion === "TO-BE-COMPLETED")
				continue;

			//if we see this emotion first time in this chat
			//give it number 1 else increment it.
			if (channelsJSON[channel][currentEmotion] == undefined) {
				channelsJSON[channel][currentEmotion] = 1;
			} else {
				channelsJSON[channel][currentEmotion] += 1;
			}
		}
		//after traversing the list of emotions and counting,
		//we don't need them as an array inside our JSON.
		delete channelsJSON[channel]["emotion"];
	}
	console.log(channelsJSON)
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
		socket.emit('server request', channelsJSON);
	}
});