package fi.lbd.mobile.mapobjects;

import android.support.annotation.NonNull;

/**
 * Basic implementation of the MapObject-interface.
 *
 * Created by tommi on 18.10.2014.
 */
public class BasicMapObject implements MapObject {
    private final String id;
    private final PointLocation pointLocation;

    public BasicMapObject(@NonNull String id, @NonNull PointLocation pointLocation) {
        this.id = id;
        this.pointLocation = pointLocation;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public PointLocation getPointLocation() {
        return this.pointLocation;
    }
}
