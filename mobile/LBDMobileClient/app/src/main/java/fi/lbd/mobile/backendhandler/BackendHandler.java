package fi.lbd.mobile.backendhandler;

import java.util.List;

import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.PointLocation;

/**
 * Interface which defines the functions of the backend handlers.
 *
 * Created by Tommi.
 */
public interface BackendHandler {
    HandlerResponse getObjectsNearLocation(PointLocation location, double range, boolean mini);
    HandlerResponse getObjectsInArea(PointLocation southWest, PointLocation northEast, boolean mini);
    HandlerResponse getMapObject(String id);
}
