package fi.lbd.mobile.mapobjects.events;

import android.support.annotation.Nullable;

import fi.lbd.mobile.authorization.AuthorizedEvent;
import fi.lbd.mobile.events.AbstractEvent;
import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Returns detailed information about a single map object.
 */
public class ReturnMapObjectEvent extends AbstractEvent {
    private final MapObject mapObject;

    public ReturnMapObjectEvent(@Nullable MapObject mapObject) {
        this.mapObject = mapObject;
    }

    public MapObject getMapObject() {
        return this.mapObject;
    }
}
