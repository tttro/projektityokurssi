package fi.lbd.mobile.backendhandler;

import android.support.annotation.NonNull;

import java.util.List;

import fi.lbd.mobile.location.PointLocation;

/**
 * Interface which defines the functions of the backend handlers.
 *
 * Created by Tommi.
 */
public interface BackendHandler {
    /**
     * Returns objects near the given location.
     *
     * @param location  Objects near this location.
     * @param range Objects inside circular area defined by this range.
     * @param mini  Should the returned objects be minimized.
     * @return
     */
    HandlerResponse getObjectsNearLocation(PointLocation location, double range, boolean mini);

    /**
     * Returns objects inside area.
     *
     * @param southWest Start location.
     * @param northEast End location.
     * @param mini  Should the returned objects be minimized.
     * @return
     */
    HandlerResponse getObjectsInArea(PointLocation southWest, PointLocation northEast, boolean mini);

    /**
     * Returns object with the given id.
     *
     * @param id    Id of the wanted object.
     * @return
     */
    HandlerResponse getMapObject(String id);

    /**
     * Search objects which has the search string in the given fields.
     *
     * @param fromFields    Fields to search from.
     * @param searchString  String to search.
     * @param limit Limit results.
     * @param mini  Results minimized.
     * @return
     */
    HandlerResponse getObjectsFromSearch(@NonNull List<String> fromFields, @NonNull String searchString,
                                                int limit, boolean mini);
}
