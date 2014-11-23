package fi.lbd.mobile.mapobjects;

import java.io.Serializable;
import java.util.Map;

/**
 *
 *
 * Created by Tommi.
 */
public interface MapObject extends Serializable {
    String getId();
    PointLocation getPointLocation();
    Map<String, String> getAdditionalProperties();
    Map<String, String> getMetadataProperties();
    boolean isMinimized();
}
