package fi.lbd.mobile.messaging.events;

import android.support.annotation.Nullable;

import java.util.List;

import fi.lbd.mobile.events.AbstractEvent;
import fi.lbd.mobile.messaging.messageobjects.MessageObject;

/**
 * Returns the requested user messages from the backend.
 * Created by Tommi on 3.1.2015.
 */
public class ReturnUserMessagesEvent extends AbstractEvent {
    private final List<MessageObject> messageObjects;

    public ReturnUserMessagesEvent(@Nullable List<MessageObject> messageObjects) {
        this.messageObjects = messageObjects;
    }

    public List<MessageObject> getMessageObjects() {
        return messageObjects;
    }
}
