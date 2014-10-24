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
public class RequestViewChangeEvent extends AbstractEvent {
    public enum ViewType {Map};
    private final ViewType type;

    public RequestViewChangeEvent(@NonNull ViewType type) {
        this.type = type;
    }
    public ViewType getViewType() {
        return this.type;
    }
}
