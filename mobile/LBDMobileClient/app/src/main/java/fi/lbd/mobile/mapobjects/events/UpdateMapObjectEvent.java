package fi.lbd.mobile.mapobjects.events;

import android.support.annotation.NonNull;

import fi.lbd.mobile.events.AbstractEvent;
import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Try to update map object details in the backend handler.
 * Created by Tommi on 10.1.2015.
 */
public class UpdateMapObjectEvent extends AbstractEvent {
    private final MapObject updatedMapObject;

    public UpdateMapObjectEvent(@NonNull MapObject updatedMapObject) {
        this.updatedMapObject = updatedMapObject;
    }

    public MapObject getUpdatedMapObject() {
        return updatedMapObject;
    }
}
