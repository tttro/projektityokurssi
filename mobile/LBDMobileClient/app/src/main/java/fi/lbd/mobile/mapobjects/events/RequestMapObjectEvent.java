package fi.lbd.mobile.mapobjects.events;

import android.support.annotation.NonNull;

import fi.lbd.mobile.events.AbstractEvent;

public class RequestMapObjectEvent extends AbstractEvent {
    private final String id;

    public RequestMapObjectEvent(@NonNull String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
