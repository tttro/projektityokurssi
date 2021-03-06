package fi.lbd.mobile.mapobjects.events;

import android.support.annotation.Nullable;

import java.util.List;

import fi.lbd.mobile.authorization.AuthorizedEvent;
import fi.lbd.mobile.events.AbstractEvent;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Event for OTTO-bus. Returns a set of map objects as a result from a request.
 *
 * Created by tommi on 22.10.2014.
 */
public class ReturnObjectsInAreaEvent extends AbstractEvent {
    private final List<MapObject> mapObjects;
    private final ImmutablePointLocation southWest;
    private final ImmutablePointLocation northEast;

    public ReturnObjectsInAreaEvent(ImmutablePointLocation southWest, ImmutablePointLocation northEast, @Nullable List<MapObject> mapObjects) {
        this.mapObjects = mapObjects;
        this.southWest = southWest;
        this.northEast = northEast;
    }

    public List<MapObject> getMapObjects() {
        return this.mapObjects;
    }


    public ImmutablePointLocation getSouthWest() {
        return this.southWest;
    }

    public ImmutablePointLocation getNorthEast() {
        return this.northEast;
    }
}
