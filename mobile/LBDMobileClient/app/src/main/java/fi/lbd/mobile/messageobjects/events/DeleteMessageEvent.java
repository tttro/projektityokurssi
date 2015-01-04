package fi.lbd.mobile.messageobjects.events;

import fi.lbd.mobile.events.AbstractEvent;

/**
 * Created by Tommi on 3.1.2015.
 */
public class DeleteMessageEvent extends AbstractEvent {
    private final String messageId;

    public DeleteMessageEvent(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }
}
