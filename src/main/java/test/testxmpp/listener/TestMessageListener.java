package test.testxmpp.listener;


import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

public class TestMessageListener implements MessageListener {
	private static final Logger LOG = Logger
			.getLogger(TestMessageListener.class);

	public void processMessage(Chat chat, Message message) {
		String from = message.getFrom();
		String body = message.getBody();
		LOG.info(String.format("< Received message '%1$s' from %2$s", body, from));
	}

}
