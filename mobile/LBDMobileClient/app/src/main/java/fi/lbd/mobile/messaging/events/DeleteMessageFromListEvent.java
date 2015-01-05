package fi.lbd.mobile.messaging.events;

import fi.lbd.mobile.events.AbstractEvent;

/**
 * Created by Ossi on 5.1.2015.
 */
public class DeleteMessageFromListEvent extends AbstractEvent{
    private String messageId;

    public DeleteMessageFromListEvent(String deleteId){
        this.messageId = deleteId;
    }
    public String getMessageId(){
        return this.messageId;
    }
}
