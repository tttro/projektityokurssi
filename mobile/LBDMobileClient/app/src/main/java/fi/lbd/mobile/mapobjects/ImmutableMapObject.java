package fi.lbd.mobile.mapobjects;

import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.location.PointLocation;

/**
 * MapObject which cannot be modified.
 *
 * Created by Tommi.
 */
public class ImmutableMapObject implements MapObject {
    private final String id;
    private final ImmutablePointLocation location; // TODO: Muita tapoja esittää??
    private final ImmutableMap<String, String> additionalProperties;
    private final ImmutableMap<String, String> metadataProperties;
    private final boolean minimized;

    //private final String strGeometryType;
    //private final ImmutableList<Double> coordinates;
    //private final String strId;
    //private final String strElemType;
   // private final String strGeometryName;

    /**
     * MapObject which cannot be modified.
     *
     * @param minimized Does the object contain all its information.
     * @param id    Objects id.
     * @param location  Objects location.
     * @param additionalProperties  Additional properties.
     * @param metadataProperties    Metadata properties.
     */
    public ImmutableMapObject(boolean minimized, @NonNull String id,
                              @NonNull ImmutablePointLocation location,
                              @NonNull Map<String, String> additionalProperties,
                              @NonNull Map<String, String> metadataProperties) {
        this.minimized = minimized;
        this.additionalProperties = ImmutableMap.copyOf(additionalProperties);
        this.metadataProperties = ImmutableMap.copyOf(metadataProperties);
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
    @Override
    public Map<String, String> getMetadataProperties() {
        return this.metadataProperties;
    }

    // Generated equals and hash: // TODO: Miten equals ja hashcode tulisi toimia jos haetaan loputkin tiedot?
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
