package fi.lbd.mobile.messaging;

import fi.lbd.mobile.messaging.messageobjects.MessageObject;

/**
 * Created by Ossi on 29.12.2014.
 *
 * Singleton to communicate, from MessageFragment to ReadMessageActivity,
 * which message was selected from the list.
 *
 */
public class MessageObjectSelectionManager {

    private static MessageObjectSelectionManager singleton;

    private MessageObjectSelectionManager() {}

    private MessageObject selectedMessageObject;

    public static void initialize() {
        if (MessageObjectSelectionManager.singleton == null) {
            MessageObjectSelectionManager.singleton = new MessageObjectSelectionManager();
        }
    }

    public static MessageObjectSelectionManager get() {
        return MessageObjectSelectionManager.singleton;
    }

    public void setSelectedMessageObject(MessageObject selectedMessageObject){
        this.selectedMessageObject = selectedMessageObject;
    }

    public MessageObject getSelectedMessageObject(){
        return this.selectedMessageObject;
    }
}
