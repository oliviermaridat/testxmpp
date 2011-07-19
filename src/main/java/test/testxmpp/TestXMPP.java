package test.testxmpp;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

public class TestXMPP {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Create the configuration for this new connection
//		ConnectionConfiguration config = new ConnectionConfiguration("127.0.0.1");
		ConnectionConfiguration config = new ConnectionConfiguration("gmail.com", 5222, "talk.google.com");
		XMPPConnection connection = new XMPPConnection(config);
		SASLAuthentication.supportSASLMechanism("PLAIN", 0);
		// Connect to the server
		try {
			connection.connect();
			// Log into the server
			connection.login("login@gmail.com", "password", "SomeResource, useless here");
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		
		// Get the user's roster
		Roster roster = connection.getRoster();
	
//		// Print the number of contacts
//		System.out.println("Number of contacts: " + roster.getEntryCount());
//		// Enumerate all contacts in the user's roster
//		for (RosterEntry entry : roster.getEntries()) {
//			System.out.println("User: " + entry.getUser());
//			// Write something to filename
//			if (entry.getUser().contains("fylhan")) {
//				Message msg = new Message(entry.getUser(), Message.Type.chat);
//				msg.setBody("Hello Fylhan!");
//				connection.sendPacket(msg);
//				System.out.println("Sent to Fylhan > Hello Fylhan!");
//			}
//		}
		
////		// Create a new presence. Pass in false to indicate we're unavailable.
//		Presence presence = new Presence(Presence.Type.unavailable);
//		presence.setStatus("Gone fishing");
//		// Send the packet
//		connection.sendPacket(presence);
//		
		ChatManager chatmanager = connection.getChatManager();
		Chat newChat = chatmanager.createChat("test@testxmpp", new MessageListener() {
		    public void processMessage(Chat chat, Message message) {
		        System.out.println("Received message: " + message);
		    }
		});

		try {
			Message msg = new Message("olivier@testxmpp", Message.Type.groupchat);
			msg.setBody("Hello Fylhan!");
		    newChat.sendMessage(msg);
		    System.out.println("Sent to Olivier > Hello Fylhan!");
		}
		catch (XMPPException e) {
		    System.out.println("Error Delivering block");
		}
		
		// Disconnect from the server
		connection.disconnect();
	}
}
