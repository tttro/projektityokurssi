package fi.lbd.mobile.mapobjects.events;

import android.support.annotation.NonNull;

import fi.lbd.mobile.events.AbstractEvent;

/**
 * Requests for details about a single map object.
 */
public class RequestMapObjectEvent extends AbstractEvent {
    private final String id;

    public RequestMapObjectEvent(@NonNull String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
