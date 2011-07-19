package test.testxmpp.listener;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

public class TestPacketListener implements PacketListener {
	private static final Logger LOG = Logger
			.getLogger(TestPacketListener.class);
	private String me;
	
	public TestPacketListener(String me) {
		this.me = me;
	}
	public void processPacket(Packet packet) {
		if (packet instanceof Message) {
			Message message = (Message) packet;
			String from = message.getFrom();
			if (!from.endsWith(me)) {
				String body = message.getBody();
				LOG.info(String.format("< Received message '%1$s' from %2$s", body, from));
			}
		}
	}

}
