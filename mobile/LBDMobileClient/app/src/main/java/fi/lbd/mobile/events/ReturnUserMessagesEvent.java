package fi.lbd.mobile.events;

import android.support.annotation.Nullable;

import java.util.List;

import fi.lbd.mobile.messageobjects.MessageObject;

/**
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
