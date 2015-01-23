package fi.lbd.mobile.backendhandler;

import android.util.Pair;

import java.util.List;

import fi.lbd.mobile.location.PointLocation;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.messaging.messageobjects.MessageObject;

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
     * @param customHeaders Headers which should be included in REST request.
     * @return
     */
    HandlerResponse<MapObject> getObjectsNearLocation(PointLocation location, double range, boolean mini, Pair<String, String>... customHeaders);

    /**
     * Returns objects inside area.
     *
     * @param southWest Start location.
     * @param northEast End location.
     * @param mini  Should the returned objects be minimized.
     * @param customHeaders Headers which should be included in REST request.
     * @return
     */
    HandlerResponse<MapObject> getObjectsInArea(PointLocation southWest, PointLocation northEast, boolean mini, Pair<String, String>... customHeaders);

    /**
     * Returns object with the given id.
     *
     * @param id    Id of the wanted object.
     * @param customHeaders Headers which should be included in REST request.
     * @return
     */
    HandlerResponse<MapObject> getMapObject(String id, Pair<String, String>... customHeaders);

    /**
     * Search objects which has the search string in the given fields.
     *
     * @param fromFields    Fields to search from.
     * @param searchString  String to search.
     * @param limit Limit results.
     * @param mini  Results minimized.
     * @param customHeaders Headers which should be included in REST request.
     * @return
     */
    HandlerResponse<MapObject> getObjectsFromSearch(List<String> fromFields, String searchString,
                                                int limit, boolean mini, Pair<String, String>... customHeaders);

    /**
     * Updates the object in the backend.
     *
     * @param updatedMapObject  Object which should be updated.
     * @param customHeaders Headers which should be included in REST request.
     * @return
     */
    HandlerResponse<MapObject> updateMapObject(MapObject updatedMapObject, Pair<String, String>... customHeaders);

    /**
     * Requests the list of authorized users.
     * @param customHeaders Headers which should be included in REST request.
     * @return
     */
    HandlerResponse<String> getUsers(Pair<String, String>... customHeaders);

    /**
     * Requests the list of object collections in the database.
     * @param url  Backend url
     * @param customHeaders Headers which should be included in REST request.
     * @return
     */
    HandlerResponse<String> getCollections(String url, Pair<String, String>... customHeaders);

    /**
     * Request messages for the current user.
     * @param customHeaders Headers which should be included in REST request.
     * @return
     */
    HandlerResponse<MessageObject> getMessages(Pair<String, String>... customHeaders);

    /**
     * Post a message
     *
     * @param receiver  Message receiver.
     * @param topic Message topic.
     * @param message   Message contents.
     * @param attachedObjectIds Attached object ids.
     * @param customHeaders Headers which should be included in REST request.
     * @return
     */
    HandlerResponse<MessageObject> postMessage(String receiver,
                                               String topic,
                                               Object message,
                                               List<String> attachedObjectIds,
                                               Pair<String, String>... customHeaders);

    /**
     * Remove message with given message id from the backend.
     *
     * @param messageId Message to remove.
     * @param customHeaders Headers which should be included in REST request.
     * @return
     */
    HandlerResponse<MessageObject> deleteMessage(String messageId, Pair<String, String>... customHeaders);

}
