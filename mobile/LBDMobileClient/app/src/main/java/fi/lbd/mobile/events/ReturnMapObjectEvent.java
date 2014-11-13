package fi.lbd.mobile.events;

import android.support.annotation.Nullable;

import java.util.List;

import fi.lbd.mobile.mapobjects.MapObject;


public class ReturnMapObjectEvent extends AbstractEvent {
    private final MapObject mapObject;

    public ReturnMapObjectEvent(@Nullable MapObject mapObject) {
        this.mapObject = mapObject;
    }

    public MapObject getMapObject() {
        return this.mapObject;
    }
}
