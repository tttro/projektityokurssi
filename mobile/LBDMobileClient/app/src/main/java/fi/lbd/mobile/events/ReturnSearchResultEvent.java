package fi.lbd.mobile.events;

import android.support.annotation.Nullable;

import java.util.List;

import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Event for OTTO-bus. Returns a set of map objects as a result from a request.
 *
 * Created by Tommi.
 */
public class ReturnSearchResultEvent extends AbstractEvent {
    private final List<MapObject> mapObjects;

    public ReturnSearchResultEvent(@Nullable List<MapObject> mapObjects) {
        this.mapObjects = mapObjects;
    }

    public List<MapObject> getMapObjects() {
        return this.mapObjects;
    }
}
