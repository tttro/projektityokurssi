package fi.lbd.mobile.messaging.events;

import android.util.Log;

import java.util.List;

import fi.lbd.mobile.events.AbstractEvent;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.messaging.messageobjects.MessageObject;

/**
 * Created by Ossi on 14.1.2015.
 */
public class UpdateCachedMessagesToListEvent extends AbstractEvent{
    private List<MessageObject> objects;

    public UpdateCachedMessagesToListEvent(List<MessageObject> objects){
        this.objects = objects;
    }

    public List<MessageObject> getObjects(){
        return this.objects;
    }
}
