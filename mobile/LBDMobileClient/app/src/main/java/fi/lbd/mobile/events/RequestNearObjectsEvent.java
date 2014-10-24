package fi.lbd.mobile.events;

import android.support.annotation.NonNull;

import fi.lbd.mobile.mapobjects.PointLocation;

/**
 * Event for OTTO-bus. Requests nearby objects from object repository service.
 *
 * Created by tommi on 19.10.2014.
 */
public class RequestNearObjectsEvent extends AbstractEvent {
    private PointLocation location;

    public RequestNearObjectsEvent(@NonNull PointLocation location) {
        this.location = location;
    }

    public PointLocation getLocation() {
        return this.location;
    }
}
