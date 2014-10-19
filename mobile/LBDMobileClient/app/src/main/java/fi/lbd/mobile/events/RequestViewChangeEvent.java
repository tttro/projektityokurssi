package fi.lbd.mobile.events;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Event for OTTO-bus. Requests view change.
 *
 * Created by tommi on 19.10.2014.
 */
public class RequestViewChangeEvent {
    public enum ViewType {Map};
    private final ViewType type;
    private final List<MapObject> mapObjects;

    public RequestViewChangeEvent(@NonNull ViewType type, @Nullable List<MapObject> mapObjects) {
        this.type = type;
        this.mapObjects = mapObjects;
    }

    public RequestViewChangeEvent(@NonNull ViewType type, @Nullable MapObject mapObject) {
        this.type = type;
        if (mapObject != null) {
            this.mapObjects = new ArrayList<MapObject>();
            this.mapObjects.add(mapObject);
        } else {
            this.mapObjects = null;
        }
    }

    public ViewType getViewType() {
        return this.type;
    }

    public List<MapObject> getMapObjects() {
        return this.mapObjects;
    }
}
