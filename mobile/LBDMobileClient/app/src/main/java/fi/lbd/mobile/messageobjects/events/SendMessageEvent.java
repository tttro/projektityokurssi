package fi.lbd.mobile.messageobjects.events;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import fi.lbd.mobile.events.AbstractEvent;
import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Created by Tommi on 3.1.2015.
 */
public class SendMessageEvent<T> extends AbstractEvent {
    private final String receiver;
    private final String topic;
    private final T message;
    private final List<String> objectAttachments;

    public SendMessageEvent(@NonNull String receiver,
                            @NonNull String topic,
                            @NonNull T message,
                            @Nullable MapObject... objects) {
        this.receiver = receiver;
        this.topic = topic;
        this.message = message;
        List<String> tempList = new ArrayList<>();
        if (objects != null && objects.length > 0) {
            for (MapObject object : objects) {
                tempList.add(object.getId());
            }
        }
        this.objectAttachments = ImmutableList.copyOf(tempList);
    }

    /*
    public SendMessageEvent(@NonNull String receiver,
                            @NonNull String topic,
                            @NonNull T message,
                            @Nullable List<MapObject> objects) {
        this.receiver = receiver;
        this.topic = topic;
        this.message = message;
        List<String> tempList = new ArrayList<>();
        if (objects != null && objects.size() > 0) {
            for (MapObject object : objects) {
                tempList.add(object.getId());
            }
        }
        this.objectAttachments = ImmutableList.copyOf(tempList);
    }

    public SendMessageEvent(@NonNull String receiver,
                            @NonNull String topic,
                            @NonNull T message,
                            @Nullable String... objectIds) {
        this.receiver = receiver;
        this.topic = topic;
        this.message = message;

        List<String> tempList = new ArrayList<>();
        if (objectIds != null && objectIds.length > 0) {
            for (String objectId : objectIds) {
                tempList.add(objectId);
            }
        }
        this.objectAttachments = ImmutableList.copyOf(tempList);
    }

*/
    public String getReceiver() {
        return receiver;
    }

    public String getTopic() {
        return topic;
    }

    public T getMessage() {
        return message;
    }

    public List<String> getObjectAttachments() {
        return objectAttachments;
    }
}
