package fi.lbd.mobile.mapobjects;

import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MapObject which cannot be modified.
 *
 * Created by tommi on 22.10.2014.
 */
public class ImmutableMapObject implements MapObject {
    private final String id;
    private final ImmutablePointLocation location; // TODO: Muita tapoja esittää??
    private final ImmutableMap<String, String> additionalProperties;
    private final boolean minimized;

    //private final String strGeometryType;
    //private final ImmutableList<Double> coordinates;
    //private final String strId;
    //private final String strElemType;
   // private final String strGeometryName;

    public ImmutableMapObject(boolean minimized, @NonNull String id, @NonNull ImmutablePointLocation location, @NonNull Map<String, String> additionalProperties) {
        this.minimized = minimized;
        this.additionalProperties = ImmutableMap.copyOf(additionalProperties);
        this.id = id;
        this.location = location;
    }

    @Override
    public boolean isMinimized() {
        return this.minimized;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public PointLocation getPointLocation() {
        return this.location;
    }

    @Override
    public Map<String, String> getAdditionalProperties() {
        return this.additionalProperties;
    }

    // Generated equals and hash:
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImmutableMapObject that = (ImmutableMapObject) o;

        if (minimized != that.minimized) return false;
        if (!additionalProperties.equals(that.additionalProperties)) return false;
        if (!id.equals(that.id)) return false;
        if (!location.equals(that.location)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + location.hashCode();
        result = 31 * result + additionalProperties.hashCode();
        result = 31 * result + (minimized ? 1 : 0);
        return result;
    }
}
