//{"emoticon_set":0,"channel_name":null,"code":"FrankerZ","emotion":"TO-BE-COMPLETED","_id":65,"channel_id":null,"timestamp":"1558381101999"}
var port = 5672;
var http = require("http");
var amqp = require('amqplib/callback_api');

var IPtoConnect = 'amqp://139.179.103.146';
var messages = [];
var emotionsJSON;

//remove messages that was sent more than 10 minutes ago.
function filterMessagesBy10Minutes() {
    messages = messages.filter(function (message) {
        return (Date.now() / (60 * 1000) - parseInt(message.timestamp) / (60 * 1000)) < 10;
    });
    groupMessagesByEmotions();
}

//Group and count the number of messages belonging to each emotion
//and assign this to emotionsJSON variable.
function groupMessagesByEmotions() {
    var key = 'emotion';
    emotionsJSON = [];
    for (var i = 0; i < messages.length; i++) {
        var added = false;
        for (var j = 0; j < emotionsJSON.length; j++) {
            if (emotionsJSON[j][key] == messages[i][key]) {
                emotionsJSON[j].items.push(messages[i]);
                added = true;
                break;
            }
        }
        if (!added) {
            var entry = {items: []};
            entry[key] = messages[i][key];
            entry.items.push(messages[i]);
            emotionsJSON.push(entry);
        }
    }
    console.log("********************************");
    for (var i = 0; i < emotionsJSON.length; i++)
        console.log(emotionsJSON[i].emotion + " - " + Object.keys(emotionsJSON[i].items).length);
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
/*
//AJAX CONNECTION
var server = http.createServer();

server.on('request', request);
server.listen(port);
function request(request, response) {
    var store = '';

    request.on('data', function(data) 
    {
        store += data;
    });
    request.on('end', function() 
    {  console.log(store);
        response.setHeader("Content-Type", "text/json");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.end(store)
	});
}  
*/