package fi.lbd.mobile.messaging.events;

import fi.lbd.mobile.authorization.AuthorizedEvent;

/**
 * Created by Ossi on 5.1.2015.
 */
public class DeleteMessageFromListEvent extends AuthorizedEvent {
    private String messageId;

    public DeleteMessageFromListEvent(String deleteId){
        this.messageId = deleteId;
    }
    public String getMessageId(){
        return this.messageId;
    }
}
