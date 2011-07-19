package test.testxmpp;

import java.util.Collection;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.MultiUserChat;

public class TestMultiUserChatXMPP implements MessageListener, PacketListener {
	private static final int packetReplyTimeout = 500; // millis
    private String server;
    private int port;
    
    private ConnectionConfiguration config;
    private XMPPConnection connection;
    private ChatManager chatManager;
    private MultiUserChat muc;
    
	public TestMultiUserChatXMPP(String server, int port) {
		this.server = server;
		this.port = port;
	}
	
	public void init() throws XMPPException {
		System.out.println(String.format("Initializing connection to server %1$s port %2$d", server, port));
		SmackConfiguration.setPacketReplyTimeout(packetReplyTimeout);
		config = new ConnectionConfiguration(server, port);
		config.setSASLAuthenticationEnabled(false);
		config.setSecurityMode(SecurityMode.disabled);
		connection = new XMPPConnection(config);
		connection.connect();
		System.out.println("Connected: " + connection.isConnected());

		chatManager = connection.getChatManager();
	}
	
	public void performLogin(String username, String password) throws XMPPException {
		if (connection!=null && connection.isConnected()) {
			connection.login(username, password);
		}
	}
	
	public void destroy() {
		if (connection!=null && connection.isConnected()) {
			connection.disconnect();
		}
	}

	public void setStatus(boolean available, String status) {

		Presence.Type type = available? Type.available: Type.unavailable;
		Presence presence = new Presence(type);
		presence.setStatus(status);
		connection.sendPacket(presence);
	}

	public void createEntry(String user, String name) throws Exception {
		System.out.println(String.format("Creating entry for buddy '%1$s' with name %2$s", user, name));
		Roster roster = connection.getRoster();
		roster.createEntry(user, name, null);
	}
	
	public void createMultiUserChat() throws XMPPException {
		muc = new MultiUserChat(connection, "myroom4@conference.testopenfire");
		// Create the room
		try {
			muc.create("Test");
			// Send an empty room configuration form which indicates that we want
			// an instant room
			muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
		}
		catch(XMPPException e) {
			muc.join("Test");
		}
		muc.addMessageListener(this);
	}
	
	public void printRoster() throws Exception {
		System.out.println("My roster :");
		Roster roster = connection.getRoster();
		Collection<RosterEntry> entries = roster.getEntries();  
		for (RosterEntry entry : entries) {
			System.out.println(String.format("Buddy:%1$s - Status:%2$s - User JID:%3$s - Type:%4$s", 
					entry.getName(), entry.getStatus(), entry.getUser(), entry.getType().toString()));
			for(RosterGroup group : entry.getGroups()) {
				System.out.println("Group: "+group.getName()+", nb:"+group.getEntryCount());
			}
		}
	}
	
	public void sendMessage(String message, String buddyJID) throws XMPPException {
		System.out.println(String.format("> Sending mesage '%1$s' to user %2$s", message, buddyJID));
		Chat chat = chatManager.createChat(buddyJID, this);
		chat.sendMessage(message);
	}
	public void sendMessage(String message, MultiUserChat muc) throws XMPPException {
		System.out.println(String.format("> Sending mesage '%1$s' to chat %2$s", message, muc.getRoom()));
		muc.sendMessage(message);
	}
	
	public void processMessage(Chat chat, Message message) {
		String from = message.getFrom();
		String body = message.getBody();
		System.out.println(String.format("< Received message '%1$s' from %2$s (chat room participant : %3$s)", body, from, chat.getParticipant()));
	}
	public void processPacket(Packet packet) {
		if (packet instanceof Message) {
			Message message = (Message) packet;
			String from = message.getFrom();
			String body = message.getBody();
			System.out.println(String.format("< Received message '%1$s' from %2$s", body, from));
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String username = "test";
        String password = "test";
        
        TestMultiUserChatXMPP xmppManager = new TestMultiUserChatXMPP("127.0.0.1", 5222);
        
        try {
			xmppManager.init();
		
	        xmppManager.performLogin(username, password);
	        xmppManager.setStatus(true, "Hello everyone");
	        
	        String buddyJIDFylhan = "fylhan@testopenfire";
	        String buddyName = "Fylhan";
	        xmppManager.createEntry(buddyJIDFylhan, buddyName);
	        String buddyJIDEliza = "eliza@testopenfire";
	        buddyName = "Eliza";
	        xmppManager.createEntry(buddyJIDEliza, buddyName);
	        xmppManager.printRoster();
	        
	        xmppManager.createMultiUserChat();
	        xmppManager.muc.invite(buddyJIDFylhan, "Join this excellent room");
	        xmppManager.muc.invite(buddyJIDEliza, "Join this excellent room");
	        xmppManager.muc.sendMessage("Hello mates !");
	        
	        boolean isRunning = true;
	        
	        while (isRunning) {
	            Thread.sleep(50);
	        }
        } catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        xmppManager.destroy();
	}
}
