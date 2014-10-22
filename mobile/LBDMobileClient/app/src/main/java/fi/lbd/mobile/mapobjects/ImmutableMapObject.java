package fi.lbd.mobile.mapobjects;

import android.support.annotation.NonNull;

/**
 * MapObject which cannot be modified.
 *
 * Created by tommi on 22.10.2014.
 */
public class ImmutableMapObject implements MapObject {
    private final String id;
    private final ImmutablePointLocation location;

    public ImmutableMapObject(@NonNull String id, @NonNull ImmutablePointLocation location) {
        this.id = id;
        this.location = location;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public PointLocation getPointLocation() {
        return this.location;
    }
}
