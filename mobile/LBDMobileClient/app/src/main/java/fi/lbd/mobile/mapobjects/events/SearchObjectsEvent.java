package fi.lbd.mobile.mapobjects.events;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import fi.lbd.mobile.events.AbstractEvent;
import fi.lbd.mobile.location.PointLocation;

/**
 * Event for OTTO-bus. Searches for objects with string.
 *
 * Created by Tommi.
 */
public class SearchObjectsEvent extends AbstractEvent {
    private final List<String> fromFields;
    private final String searchString;
    private final int limit;
    private final boolean mini;

    public SearchObjectsEvent(@NonNull List<String> fromFields, @NonNull String searchString,
                              int limit, boolean mini) {
        this.fromFields = fromFields;
        this.searchString = searchString;
        this.limit = limit;
        this.mini = mini;
    }

    public SearchObjectsEvent(@NonNull String searchString,
                              int limit, boolean mini, @NonNull String... fromFields) {
        this.fromFields = new ArrayList<>();
        for (String field: fromFields) {
            this.fromFields.add(field);
        }
        this.searchString = searchString;
        this.limit = limit;
        this.mini = mini;
    }

    public List<String> getFromFields() {
        return this.fromFields;
    }

    public String getSearchString() {
        return this.searchString;
    }

    public int getLimit() {
        return this.limit;
    }

    public boolean isMini() {
        return this.mini;
    }
}
