package fi.lbd.mobile.messaging;

import com.squareup.otto.Produce;

import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.messaging.events.DeleteMessageFromListEvent;

/**
 * Created by Ossi on 5.1.2014
 *
 * Singleton to communicate, from ReadMessageActivity to MessageFragment, which message was
 * successfully removed, so that MessageFragment can remove the message from UI.
 *
 * @Produce produces a DeleteMessageFromListEvent when called from ReadMessageActivity.
 * This event is subscribed to in MessageFragment.
 */
public class MessageObjectDeletionManager {

    private static MessageObjectDeletionManager singleton;

    private MessageObjectDeletionManager() {
        BusHandler.getBus().register(this);
    }

    private String deletedMessageObject;

    public static void initialize() {
        if (MessageObjectDeletionManager.singleton == null) {
            MessageObjectDeletionManager.singleton = new MessageObjectDeletionManager();
        }
    }

    public static MessageObjectDeletionManager get() {
        return MessageObjectDeletionManager.singleton;
    }

    public void setDeletedMessageObject(String deletedObject){
        this.deletedMessageObject = deletedObject;
    }

    public String getDeletedMessageObject(){
        return this.deletedMessageObject;
    }

    @Produce
    public DeleteMessageFromListEvent deleteMessageFromList(){
        return new DeleteMessageFromListEvent(getDeletedMessageObject());
    }
}