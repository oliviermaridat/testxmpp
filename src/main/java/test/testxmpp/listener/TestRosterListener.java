package test.testxmpp.listener;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

public class TestRosterListener implements RosterListener {
	private static final Logger LOG = Logger
			.getLogger(TestRosterListener.class);
	
	private XMPPConnection connection;
	
	public TestRosterListener(XMPPConnection connection) {
		this.connection = connection;
	}
	
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
	
	public void printRoster() {
		LOG.info("My roster:");
		Roster roster = connection.getRoster();
		for (RosterEntry entry : roster.getEntries()) {
			String onlineStatus = "offline";
			Presence presence = roster.getPresence(entry.getUser());
		    if (presence.getType().equals(Presence.Type.available)) {
		    	onlineStatus = "online";
		    }
			String rosterLine = entry.getName()+" ("+entry.getUser()+") ["+onlineStatus+"] {Status:"+entry.getStatus()+", Type:"+entry.getType();
			for(RosterGroup group : entry.getGroups()) {
				rosterLine += ", Group: "+group.getName()+" (nb:"+group.getEntryCount()+")";
			}
			rosterLine += "}";
			LOG.info(rosterLine);
		}
	}
}
