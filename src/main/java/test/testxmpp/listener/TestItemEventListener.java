package test.testxmpp.listener;

import org.apache.log4j.Logger;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

public class TestItemEventListener implements ItemEventListener<Item> {
	private static final Logger LOG = Logger.getLogger(TestItemEventListener.class);
	
	public TestItemEventListener() {
		
	}
	
    public void handlePublishedItems(ItemPublishEvent<Item> items) {
    	LOG.info("Item count: " + items.getItems().size());
    	LOG.info(items);
    	for (Item item : items.getItems()) {
    		LOG.info(item.getNamespace()+":"+item.getElementName()+" : "+item.getNode()+item.getId());
    	}
	}
}
