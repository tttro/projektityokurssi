package fi.lbd.mobile.backendhandler;

import java.util.List;

import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.PointLocation;

/**
 * Created by tommi on 10.11.2014.
 */
public interface BackendHandler {
    List<MapObject> getObjectsNearLocation(PointLocation location, double range, boolean mini);
    List<MapObject> getObjectsInArea(PointLocation southWest, PointLocation northEast, boolean mini);
    MapObject getMapObject(String id);
}
