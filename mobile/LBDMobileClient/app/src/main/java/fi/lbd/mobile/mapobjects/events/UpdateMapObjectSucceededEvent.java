package fi.lbd.mobile.mapobjects.events;

import android.support.annotation.NonNull;

import fi.lbd.mobile.authorization.AuthorizedEvent;
import fi.lbd.mobile.events.AbstractEvent;
import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Updating map object details succeeded.
 * Created by Tommi on 10.1.2015.
 */
public class UpdateMapObjectSucceededEvent extends AbstractEvent {
    private final MapObject updatedMapObject;

    public UpdateMapObjectSucceededEvent(@NonNull MapObject updatedMapObject) {
        this.updatedMapObject = updatedMapObject;
    }

    public MapObject getUpdatedMapObject() {
        return updatedMapObject;
    }
}
