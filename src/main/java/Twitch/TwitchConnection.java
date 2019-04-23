package Twitch;

import Util.Secrets;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;

public class TwitchConnection {
	private static TwitchClient twitchClient = TwitchClientBuilder.builder()
			.withChatAccount(new OAuth2Credential("twitch", Secrets.TWITCH_TOKEN))
			.withEnableChat(true)
			.withEventManager(new EventManager())
			.build();

	public static TwitchClient getTwitchClient() {
		return twitchClient;
	}

	private TwitchConnection() {
	}
}
