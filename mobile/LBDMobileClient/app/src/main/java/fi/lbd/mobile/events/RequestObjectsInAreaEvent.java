package fi.lbd.mobile.events;

import com.google.android.gms.maps.model.LatLngBounds;

import fi.lbd.mobile.mapobjects.ImmutablePointLocation;

public class RequestObjectsInAreaEvent extends AbstractEvent {
    private final ImmutablePointLocation southWest;
    private final ImmutablePointLocation northEast;
    private final boolean minimized;

    public RequestObjectsInAreaEvent(ImmutablePointLocation southWest, ImmutablePointLocation northEast) {
        this(southWest, northEast, true);
    }

    public RequestObjectsInAreaEvent(ImmutablePointLocation southWest, ImmutablePointLocation northEast, boolean minimized) {
        this.southWest = southWest;
        this.northEast = northEast;
        this.minimized = minimized;
    }

    public ImmutablePointLocation getSouthWest() {
        return this.southWest;
    }

    public ImmutablePointLocation getNorthEast() {
        return this.northEast;
    }

    public boolean isMinimized() {
        return this.minimized;
    }
}
