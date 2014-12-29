package fi.lbd.mobile;

import fi.lbd.mobile.messageobjects.MessageObject;

/**
 * Created by Ossi on 29.12.2014.
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
