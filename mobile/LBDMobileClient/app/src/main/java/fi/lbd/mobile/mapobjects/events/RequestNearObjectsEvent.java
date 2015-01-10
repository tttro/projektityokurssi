package fi.lbd.mobile.mapobjects.events;

import android.support.annotation.NonNull;

import fi.lbd.mobile.events.AbstractEvent;
import fi.lbd.mobile.location.PointLocation;

/**
 * Event for OTTO-bus. Requests nearby objects from object repository service.
 *
 * Created by tommi on 19.10.2014.
 */
public class RequestNearObjectsEvent extends AbstractEvent {
    private final PointLocation location;
    private final boolean minimized;
    private final double range;

    public RequestNearObjectsEvent(@NonNull PointLocation location, double range) {
        this(location, range, true);
    }
    public RequestNearObjectsEvent(@NonNull PointLocation location, double range, boolean minimized) {
        this.location = location;
        this.minimized = minimized;
        this.range = range;
    }

    public PointLocation getLocation() {
        return this.location;
    }

    public boolean isMinimized() {
        return this.minimized;
    }

    public double getRange() {
        return this.range;
    }
}
