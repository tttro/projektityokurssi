package fi.lbd.mobile.events;

import android.support.annotation.NonNull;

import fi.lbd.mobile.mapobjects.PointLocation;

public class RequestMapObjectEvent extends AbstractEvent {
    private final String id;

    public RequestMapObjectEvent(@NonNull String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
