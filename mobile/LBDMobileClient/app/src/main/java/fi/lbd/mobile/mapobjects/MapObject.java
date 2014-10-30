package fi.lbd.mobile.mapobjects;

import java.util.Map;

/**
 *
 *
 * Created by tommi on 18.10.2014.
 */
public interface MapObject {
    String getId();
    PointLocation getPointLocation();
    Map<String, String> getAdditionalProperties();
}
