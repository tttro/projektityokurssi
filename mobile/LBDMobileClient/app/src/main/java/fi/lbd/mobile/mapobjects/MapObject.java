package fi.lbd.mobile.mapobjects;

import java.io.Serializable;
import java.util.Map;

import fi.lbd.mobile.location.PointLocation;

/**
 * Interface for map objects.
 *
 * Created by Tommi.
 */
public interface MapObject extends Serializable {
    /**
     * Returns map objects id.
     *
     * @return
     */
    String getId();

    /**
     * Returns map objects location.
     *
     * @return
     */
    PointLocation getPointLocation();

    /**
     * Returns a map of the objects properties.
     *
     * @return
     */
    Map<String, String> getAdditionalProperties();

    /**
     * Returns a map of the objects metadata
     *
     * @return
     */
    Map<String, String> getMetadataProperties();

    /**
     * Returns true if the object is in minimized format and doesn't contain all the information
     * the object actually has.
     *
     * @return
     */
    boolean isMinimized();
}
