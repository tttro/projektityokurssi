package fi.lbd.mobile.mapobjects.events;

import fi.lbd.mobile.authorization.AuthorizedEvent;
import fi.lbd.mobile.location.ImmutablePointLocation;

/**
 * Cache objects inside an area but don't return them.
 */
public class CacheObjectsInAreaEvent extends AuthorizedEvent {
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
