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
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.FormType;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;

public class Provider {
	private static final Logger LOG = Logger.getLogger(Provider.class);
	
    private String server;
    private String username;
    private String password;
    private String resource;
    private static String defaultServer = "127.0.0.1";
    private static String defaultUsername = "test";
    private static String defaultPassword = "test";
    private static String defaultResource = "SomeComputer";
    private static final int packetReplyTimeout = 500; // milliseconds
    
    private ConnectionConfiguration config;
    private XMPPConnection connection;
    
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
		XMPPConnection.DEBUG_ENABLED = cmd.hasOption('d');
		
		Provider provider = null;
        try {
        	provider = new Provider(server, username, password, resource);
        	provider.start();
        	
        	// Create a pubsub manager using an existing Connection
            PubSubManager mgr = new PubSubManager(provider.connection);

            // Create the node
            ConfigureForm form = new ConfigureForm(FormType.submit);
            form.setAccessModel(AccessModel.open);
            form.setDeliverPayloads(false);
            form.setNotifyRetract(true);
            form.setPersistentItems(true);
            form.setPublishModel(PublishModel.open);
            LeafNode leaf = (LeafNode) mgr.createNode("testNode4", form);
            leaf.send(new PayloadItem("test" + System.currentTimeMillis(), 
                    new SimplePayload("book", "pubsub:test:book", "")));
        	
	        LOG.info("Enter your message");
			LOG.info("Say \"bye\" to quit");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String msg;
			while (!(msg = br.readLine()).equals("bye")) {
				leaf.send(new PayloadItem(msg + System.currentTimeMillis(), 
	                    new SimplePayload("book", "pubsub:test:book", "")));
			}
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
        	if (null != provider) {
        		provider.destroy();
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
		formatter.printHelp("java -jar testxmpp-bin.jar [-s server] [-u username] [-p password] [-r resource] [-d] ", options);
	}
	
	public Provider(String server, String username, String password, String resource) throws XMPPException {
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
}
