package fi.lbd.mobile.events;

import fi.lbd.mobile.location.ImmutablePointLocation;

public class CacheObjectsInAreaEvent extends AbstractEvent {
    private final ImmutablePointLocation southWest;
    private final ImmutablePointLocation northEast;
    private final boolean minimized;

    public CacheObjectsInAreaEvent(ImmutablePointLocation southWest, ImmutablePointLocation northEast) {
        this(southWest, northEast, true);
    }

    public CacheObjectsInAreaEvent(ImmutablePointLocation southWest, ImmutablePointLocation northEast, boolean minimized) {
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
