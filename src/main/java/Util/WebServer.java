package Util;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.Map;

public class WebServer extends NanoHTTPD {


	public WebServer(int port) throws IOException {
		super(port);
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		System.out.println("\nRunning! Point your browsers to http://localhost:" + port + "/ \n");

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

		}
		return newFixedLengthResponse(msg + "</body></html>\n");
	}
}