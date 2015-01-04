package fi.lbd.mobile.backendhandler;

import android.support.annotation.NonNull;

import java.util.List;

import fi.lbd.mobile.location.PointLocation;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.messageobjects.MessageObject;
import fi.lbd.mobile.messageobjects.StringMessageObject;

/**
 * Interface which defines the functions of the backend handlers.
 *
 * Created by Tommi.
 */
public interface BackendHandler {
    /**
     * Returns objects near the given location.
     *
     * @param dataSource Data source which should be used.
     * @param location  Objects near this location.
     * @param range Objects inside circular area defined by this range.
     * @param mini  Should the returned objects be minimized.
     * @return
     */
    HandlerResponse<MapObject> getObjectsNearLocation(String dataSource, PointLocation location, double range, boolean mini);

    /**
     * Returns objects inside area.
     *
     * @param dataSource Data source which should be used.
     * @param southWest Start location.
     * @param northEast End location.
     * @param mini  Should the returned objects be minimized.
     * @return
     */
    HandlerResponse<MapObject> getObjectsInArea(String dataSource, PointLocation southWest, PointLocation northEast, boolean mini);

    /**
     * Returns object with the given id.
     *
     * @param dataSource Data source which should be used.
     * @param id    Id of the wanted object.
     * @return
     */
    HandlerResponse<MapObject> getMapObject(String dataSource, String id);

    /**
     * Search objects which has the search string in the given fields.
     *
     * @param dataSource Data source which should be used.
     * @param fromFields    Fields to search from.
     * @param searchString  String to search.
     * @param limit Limit results.
     * @param mini  Results minimized.
     * @return
     */
    HandlerResponse<MapObject> getObjectsFromSearch(String dataSource, List<String> fromFields, String searchString,
                                                int limit, boolean mini);


    /**
     * Request messages for the current user.
     * @return
     */
    HandlerResponse<MessageObject> getMessages();


    /**
     * Post a message
     *
     * @param dataSource Data source which should be used.
     * @param receiver  Message receiver.
     * @param topic Message topic.
     * @param message   Message contents.
     * @param attachedObjectIds Attached object ids.
     * @return
     */
    HandlerResponse<MessageObject> postMessage(String dataSource,
                                               String receiver,
                                               String topic,
                                               Object message,
                                               List<String> attachedObjectIds);

    /**
     * Remove message with given message id from the backend.
     *
     * @param dataSource Data source which should be used.
     * @param messageId Message to remove.
     * @return
     */
    HandlerResponse<MessageObject> deleteMessage(String dataSource, String messageId);
}
