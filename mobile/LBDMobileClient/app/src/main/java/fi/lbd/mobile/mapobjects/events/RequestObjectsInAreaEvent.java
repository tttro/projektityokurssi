package fi.lbd.mobile.mapobjects.events;

import fi.lbd.mobile.events.AbstractEvent;
import fi.lbd.mobile.location.ImmutablePointLocation;

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