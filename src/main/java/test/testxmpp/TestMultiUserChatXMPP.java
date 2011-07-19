package test.testxmpp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.MultiUserChat;

import test.testxmpp.listener.TestPacketListener;
import test.testxmpp.listener.TestRosterListener;

public class TestMultiUserChatXMPP {
private static final Logger LOG = Logger.getLogger(TestMultiUserChatXMPP.class);
	
    private String server;
    private String username;
    private String password;
    private String resource;
    private String pseudonym;
    private static String defaultServer = "127.0.0.1";
    private static String defaultUsername = "test";
    private static String defaultPassword = "test";
    private static String defaultResource = "SomeComputer";
    private static String defaultPseudonym = "Test";
    private static final int packetReplyTimeout = 500; // milliseconds
    
    private ConnectionConfiguration config;
    private XMPPConnection connection;
    private Roster roster;
    
    
    /**
	 * @param args
	 */
	public static void main(String[] args) {
		CommandLine cmd = getCommandLine(args);
		String server = cmd.hasOption('s') ? cmd.getOptionValue('s')
				: defaultServer;
		String username = cmd.hasOption('u') ? cmd.getOptionValue('u')
				: defaultUsername;
		String password = cmd.hasOption('p') ? cmd.getOptionValue('p')
				: defaultPassword;
		String resource = cmd.hasOption('r') ? cmd.getOptionValue('r')
				: defaultResource;
		String pseudonym = cmd.hasOption('n') ? cmd.getOptionValue('n')
				: defaultPseudonym;
		XMPPConnection.DEBUG_ENABLED = cmd.hasOption('d');
		
		TestMultiUserChatXMPP xmppManager = null;
        try {
        	xmppManager = new TestMultiUserChatXMPP(server, username, password, resource);
        	xmppManager.start();
        	
        	// -- Change presence
	        xmppManager.setStatus(true, "Hello everyone");
	        
	        // -- Choose a contact to speak with
	        String contactJidFylhan = "fylhan@testopenfire";
	        String contactNameFylhan = "Fylhan";
	        if (null == xmppManager.roster.getEntry(contactJidFylhan)) {
	        	LOG.debug(String.format("Creating entry for '%1$s' with name %2$s", contactJidFylhan, contactNameFylhan));
	        	xmppManager.roster.createEntry(contactJidFylhan, contactNameFylhan, null);
	        }
	        String contactJidEliza = "eliza@testopenfire";
	        String contactNameEliza = "Eliza";
	        if (null == xmppManager.roster.getEntry(contactJidEliza)) {
	        	LOG.debug(String.format("Creating entry for '%1$s' with name %2$s", contactJidEliza, contactNameEliza));
	        	xmppManager.roster.createEntry(contactJidEliza, contactNameEliza, null);
	        }
	        
	        // -- Chat
	        MultiUserChat chat = new MultiUserChat(xmppManager.connection, "myroom4@conference.testopenfire");
			// Create the room
			try {
				chat.create(pseudonym);
				// Send an empty room configuration form which indicates that we want
				// an instant room
				chat.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
			}
			// Else : join it
			catch(XMPPException e) {
				chat.join(pseudonym);
			}
			chat.addMessageListener(new TestPacketListener(pseudonym));
			
			chat.invite(contactJidFylhan, "Join this excellent room");
			chat.invite(contactJidEliza, "Join this excellent room");
        	LOG.info("Enter your message");
			LOG.info("Say \"bye\" to quit");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String msg;
			while (!(msg = br.readLine()).equals("bye")) {
				xmppManager.sendMessage(chat, msg);
			}
			xmppManager.sendMessage(chat, "bye");
			// -- Change presence
	        xmppManager.setStatus(false, "Bye !");
        }
        catch (XMPPException e) {
			e.printStackTrace();
        }
	    catch (IOException e) {
			e.printStackTrace();
		}
        catch (Exception e) {
			e.printStackTrace();
		}
        finally {
        	if (null != xmppManager) {
        		xmppManager.destroy();
        	}
        }
	}
	
	protected static CommandLine getCommandLine(String[] args) {
		Options options = new Options();
		options.addOption("s", true,
				"server where the account exists (defaults to "+defaultServer+")");
		options.addOption("u", true,
				"the username (defaults to "+defaultUsername+")");
		options.addOption("p", true,
				"password for that login name (defaults to "+defaultPassword+")");
		options.addOption("r", true,
				"resource you are connecting from (default: "+defaultResource+")");
		options.addOption("n", true,
				"the pseudonym for the conference chat (default: "+defaultPseudonym+")");
		options.addOption("d", false, "enable debug (default: false)");
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			printHelp(options);
			System.exit(1);
		}

		if (cmd.hasOption('h')) {
			printHelp(options);
			System.exit(0);
		}
		return cmd;
	}
	
	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar testxmpp-bin.jar [-s server] [-u username] [-p password] [-r resource] [-n pseudonym] [-d] ", options);
	}
	
	public TestMultiUserChatXMPP(String server, String username, String password, String resource) throws XMPPException {
		this.server = server;
		this.username = username;
		this.password = password;
		this.resource = resource;
		initXMPPConnection();
	}
	
	public void initXMPPConnection() {
		// Prepare configuration
		int port = 5222;
		SmackConfiguration.setPacketReplyTimeout(packetReplyTimeout);
		config = new ConnectionConfiguration(server, port);
		config.setSASLAuthenticationEnabled(false);
		config.setSecurityMode(SecurityMode.disabled);
		// Create connection
		connection = new XMPPConnection(config);
		LOG.debug(String.format("Connection initialized to server %1$s port %2$d", server, port));
	}
	
	public void start() throws XMPPException {
		// Connect and log
		connection.connect();
		performLogin(username, password, resource);
		LOG.debug("Connected and logged: " + connection.isConnected());

		// Add listeners
		roster = connection.getRoster();
		roster.addRosterListener(new TestRosterListener(connection));
	}

	public void performLogin(String username, String password, String resource) throws XMPPException {
		if (null != connection && connection.isConnected()) {
			connection.login(username, password, resource);
		}
	}
	
	public void destroy() {
		if (null != connection && connection.isConnected()) {
			connection.disconnect();
			LOG.debug("You are disconnected");
		}
	}

	public void setStatus(boolean available, String status) {
		Presence.Type type = available ? Type.available : Type.unavailable;
		Presence presence = new Presence(type);
		presence.setStatus(status);
		connection.sendPacket(presence);
	}
	
	public void sendMessage(Chat chat, String message) throws XMPPException {
		LOG.debug(String.format("> Sending mesage '%1$s' to %2$s", message, chat.getParticipant()));
		chat.sendMessage(message);
	}
	public void sendMessage(MultiUserChat muc, String message) throws XMPPException {
		LOG.debug(String.format("> Sending mesage '%1$s' to chat %2$s", message, muc.getRoom()));
		muc.sendMessage(message);
	}
}
