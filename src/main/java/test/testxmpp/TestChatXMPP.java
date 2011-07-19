package test.testxmpp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PrivacyListManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.Privacy;
import org.jivesoftware.smack.packet.PrivacyItem;
import org.jivesoftware.smack.packet.PrivacyItem.PrivacyRule;

public class TestChatXMPP implements MessageListener {
	private static final int packetReplyTimeout = 500; // millis
    private String server;
    private int port;
    
    private ConnectionConfiguration config;
    private XMPPConnection connection;
    private ChatManager chatManager;
    private Roster roster;
    
	public TestChatXMPP(String server, int port) {
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
			roster = connection.getRoster();
			roster.addRosterListener(new RosterListener() {
				public void entriesAdded(Collection<String> arg0) {
					try {
						printRoster();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				public void entriesDeleted(Collection<String> arg0) {
					try {
						printRoster();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				public void entriesUpdated(Collection<String> arg0) {
					try {
						printRoster();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				public void presenceChanged(Presence arg0) {
					try {
						printRoster();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
		    });
		}
	}
	
	public void destroy() {
		System.out.println("You are disconnected");
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
	public boolean isInRoster(String user) throws Exception {
		System.out.print(String.format("'%1$s' is in my roster ? ", user));
		Roster roster = connection.getRoster();
		for (RosterEntry entry : roster.getEntries()) {
			if (user.equals(entry.getUser())) {
				System.out.println(true);
				return true;
			}
		}
		System.out.println(false);
		return false;
	}
	
	public void printRoster() throws Exception {
		System.out.println("My roster :");
		Roster roster = connection.getRoster();
		Collection<RosterEntry> entries = roster.getEntries();  
		for (RosterEntry entry : entries) {
			String onlineStatus = "offline";
			Presence presence = roster.getPresence(entry.getUser());
		    if (presence.getType().equals(Presence.Type.available)) {
		    	onlineStatus = "online";
		    }
			System.out.print(entry.getName()+" ("+entry.getUser()+") ["+onlineStatus+"] {Status:"+entry.getStatus()+", Type:"+entry.getType());
			for(RosterGroup group : entry.getGroups()) {
				System.out.print(", Group: "+group.getName()+" (nb:"+group.getEntryCount()+")");
			}
			System.out.println("}");
		}
	}
	
	public void sendMessage(String message, String buddyJID) throws XMPPException {
		System.out.println(String.format("> Sending mesage '%1$s' to user %2$s", message, buddyJID));
		Chat chat = chatManager.createChat(buddyJID, this);
		chat.sendMessage(message);
	}
	
	public void processMessage(Chat chat, Message message) {
		String from = message.getFrom();
		String body = message.getBody();
		System.out.println(String.format("< Received message '%1$s' from %2$s", body, from));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String username = "test";
        String password = "test";
        
        TestChatXMPP xmppManager = new TestChatXMPP("127.0.0.1", 5222);
        try {
			xmppManager.init();
	        xmppManager.performLogin(username, password);
	        xmppManager.setStatus(true, "Hello everyone");
	        
	        String buddyJID = "fylhan@testopenfire";
	        String buddyName = "Fylhan";
	        if (!xmppManager.isInRoster(buddyJID)) {
	        	xmppManager.createEntry(buddyJID, buddyName);
	        }
	        xmppManager.printRoster();
	        
	        System.out.println(buddyJID+" is now blocked");
	        // Create a privacyItem to block buddyJID
	        List<PrivacyItem> privacyItems = new ArrayList<PrivacyItem>();
//	    	PrivacyItem item = new PrivacyItem("jid", false, 1);
//	    	item.setValue(buddyJID);
//	    	privacyItems.add(item);
	    	PrivacyItem item = new PrivacyItem("subscription", false, 1);
	    	item.setValue(PrivacyRule.SUBSCRIPTION_BOTH);
	    	privacyItems.add(item);
	    	
	    	// Add it
	    	PrivacyListManager privacyManager = PrivacyListManager.getInstanceFor(xmppManager.connection);
	    	privacyManager.createPrivacyList("blockedPeople", privacyItems);
	    	privacyManager.setDefaultListName("blockedPeople");
	    	
	    
	        try {
				System.out.println("Enter your message and say \"bye\" to quit.");
				xmppManager.sendMessage("Hello mate", buddyJID);
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String msg;
				while (!(msg = br.readLine()).equals("bye")) {
					if (msg.equals("unblock")) {
						privacyManager.declineDefaultList();
						privacyManager.deletePrivacyList("blockedPeople");
						System.out.println(buddyJID+" is now unblocked");
					}
					else {
						xmppManager.sendMessage(msg, buddyJID);
					}
				}
				xmppManager.sendMessage("bye", buddyJID);
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
        } catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        finally {
        	xmppManager.destroy();
        }
	}
}
