//{ emoticon_set: 0,
//   channel_name: null,
//   code: 'PogChamp',
//   emotion: 'excited',
//   channelOfMessage: 'cohhcarnage',
//   _id: 88,
//   channel_id: null,
//   timestamp: '1558448502386' }
var port = 5672;
var amqp = require('amqplib/callback_api');
var groupBy = require('json-groupby')

var IPtoConnect = 'amqp://139.179.248.141';
var channelsJSON;
var messages = [];

//remove messages that was sent more than 10 minutes ago.
function filterMessagesBy10Minutes() {
    messages = messages.filter(function (message) {
        return (Date.now() - parseInt(message.timestamp)) / (60 * 1000) < 10;
    });
    groupMessagesByEmotions();

}

//Group and count the number of messages belonging to each emotion
//and assign this to emotionsJSON variable.
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
        //console.log(channelsJSON[channel])
    }
    console.log(channelsJSON)
}

amqp.connect(IPtoConnect, function (error0, connection) {

    setInterval(filterMessagesBy10Minutes, 5 * 1000) // filter messages every 5 minutes

    if (error0) {
        throw error0;
    }
    connection.createChannel(function (error1, channel) {
        if (error1) {
            throw error1;
        }

        var queue = 'hello';

        channel.assertQueue(queue, {
            durable: false
        });

        console.log(" [*] Waiting for messages in %s. To exit press CTRL+C", queue);

        channel.consume(queue, function (msg) {
            var message = JSON.parse(msg.content.toString());
            messages.push(message);
            //console.log(JSON.stringify(message));
        }, {
            noAck: true
        });
    });
});