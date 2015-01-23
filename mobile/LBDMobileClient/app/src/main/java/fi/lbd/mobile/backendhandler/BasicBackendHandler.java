package fi.lbd.mobile.backendhandler;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fi.lbd.mobile.backendhandler.url.UrlProvider;
import fi.lbd.mobile.backendhandler.url.UrlReader;
import fi.lbd.mobile.backendhandler.url.UrlResponse;
import fi.lbd.mobile.location.PointLocation;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.messaging.messageobjects.MessageObject;

/**
 * Handles the map objects. Fetches the requested objects from backend service.
 *
 * Created by Tommi.
 */
public class BasicBackendHandler implements BackendHandler {
    protected static final int RETRY_AMOUNT = 1;
    protected final UrlProvider urlProvider;
    protected final UrlReader urlReader;

    /**
     * Handles the map objects. Fetches the requested objects from backend service.
     *
     * @param urlReader
     * @param urlProvider
     */
    public BasicBackendHandler(UrlReader urlReader, UrlProvider urlProvider) {
        this.urlReader = urlReader;
        this.urlProvider = urlProvider;
    }

    @Override
	public HandlerResponse<MapObject> getObjectsNearLocation(PointLocation location, double range, boolean mini, Pair<String, String>... customHeaders) {
        StringBuilder str = new StringBuilder();
        str.append(this.urlProvider.getBaseObjectUrl());
        str.append(this.urlProvider.getObjectCollection());
        str.append("/");
        str.append("near/");
        str.append("?latitude=");
        str.append(location.getLatitude());
        str.append("&longitude=");
        str.append(location.getLongitude());
        if (mini) {
            str.append("&mini=true");
        }
        if (range>0) {
            str.append("&range=");
            str.append(range);
        }
        String url = str.toString();
        UrlResponse response = this.getUrl(url, RETRY_AMOUNT, customHeaders);

        // Only if the url returns code 200, we can parse the results.
        if (response != null && response.getStatus() == UrlResponse.ResponseStatus.STATUS_200) {
            List<MapObject> objects;
            try {
                objects = MapObjectParser.parseCollection(response.getContents(), mini);
            } catch (JSONException e) {
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            } catch (IOException e) {
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            }
            return new HandlerResponse<>(objects, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to get objects near location, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to get objects near location, response: "+ response);
	}

    @Override
    public HandlerResponse<MapObject> getObjectsFromSearch(@NonNull List<String> fromFields, @NonNull String searchString,
                                                int limit, boolean mini, Pair<String, String>... customHeaders) {
        StringBuilder str = new StringBuilder();
        str.append(this.urlProvider.getBaseObjectUrl());
        str.append(this.urlProvider.getObjectCollection());
        str.append("/");
        str.append("search/");
        if (mini) {
            str.append("&mini=true");
        }
        String url = str.toString();

        StringBuilder fields = new StringBuilder();
        Iterator<String> iter = fromFields.iterator();
        while (iter.hasNext()) {
            String field = iter.next();
            fields.append(field);
            if (iter.hasNext()) {
                fields.append(",");
            }
        }

        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode jsonObj = factory.objectNode()
                .put("from", fields.toString())
                .put("search", searchString)
                .put("limit", limit);
        UrlResponse response = this.urlReader.postJson(url, jsonObj.toString(), customHeaders);

        // Only if the url returns code 200, we can parse the results.
        if (response != null && response.getStatus() == UrlResponse.ResponseStatus.STATUS_200) {
            List<MapObject> objects;
            try {
                objects = MapObjectParser.parseSearchResult(response.getContents(), mini);
            } catch (JSONException e) {
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            } catch (IOException e) {
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            }
            return new HandlerResponse<>(objects, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to get objects from search, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to get objects from search, response: "+ response);
    }


    @Override
    public HandlerResponse<MapObject> getObjectsInArea(PointLocation southWest, PointLocation northEast, boolean mini, Pair<String, String>... customHeaders) {
        StringBuilder str = new StringBuilder();
        str.append(this.urlProvider.getBaseObjectUrl());
        str.append(this.urlProvider.getObjectCollection());
        str.append("/");
        str.append("inarea/");
        str.append("?xbottomleft=");
        str.append(southWest.getLongitude());
        str.append("&ybottomleft=");
        str.append(southWest.getLatitude());
        str.append("&ytopright=");
        str.append(northEast.getLatitude());
        str.append("&xtopright=");
        str.append(northEast.getLongitude());
        if (mini) {
            str.append("&mini=true");
        }
        String url = str.toString();
        UrlResponse response = this.getUrl(url, RETRY_AMOUNT, customHeaders);

        // Only if the url returns code 200, we can parse the results.
        if (response != null && response.getStatus() == UrlResponse.ResponseStatus.STATUS_200) {
            List<MapObject> objects;
            try {
                objects = MapObjectParser.parseCollection(response.getContents(), mini);
            } catch (JSONException e) {
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            } catch (IOException e) {
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            }
            return new HandlerResponse<>(objects, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to get objects in area, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to get objects in area, response: "+ response);
    }

    @Override
    public HandlerResponse<MapObject> getMapObject(String id, Pair<String, String>... customHeaders) {
        String url = this.urlProvider.getBaseObjectUrl() + this.urlProvider.getObjectCollection() + "/"+ id;
        UrlResponse response = this.getUrl(url, RETRY_AMOUNT, customHeaders);

        // Only if the url returns code 200, we can parse the results.
        if (response != null && response.getStatus() == UrlResponse.ResponseStatus.STATUS_200) {
            MapObject mapObject;

            try {
                mapObject = MapObjectParser.parse(response.getContents());
            } catch (JSONException e){
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            } catch (IOException e){
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            }
            List<MapObject> list = new ArrayList<>();
            list.add(mapObject);


            return new HandlerResponse<>(list, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to get object, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to get object, response: "+ response);
    }

    /**
     * Updates the given map object to the backend, gets the object with the updated object if from
     * the backed and checks if the update has succeeded.
     *
     * @param updatedMapObject  Object which should be updated.
     * @return
     */
    @Override
    public HandlerResponse<MapObject> updateMapObject(@NonNull MapObject updatedMapObject, Pair<String, String>... customHeaders) {
        String url = this.urlProvider.getBaseObjectUrl() + this.urlProvider.getObjectCollection() +"/"+ updatedMapObject.getId();

        String objectJson = MapObjectParser.createJsonFromObject(updatedMapObject);

//        Log.d(this.getClass().getSimpleName(), "SENDING URL: "+ url);
//        Log.d(this.getClass().getSimpleName(), "SENDING JSON: "+ objectJson);
        // Update object to backend
        UrlResponse response = this.urlReader.putJson(url, objectJson, customHeaders);

        if (response != null && response.getStatus() == UrlResponse.ResponseStatus.STATUS_200) {
            // If the update operation succeeded, fetch object with the id from backend
            HandlerResponse<MapObject> handlerResponse = this.getMapObject(updatedMapObject.getId());

            // If a single object was not found or the get operation failed, return error response.
            if (!handlerResponse.isOk() || handlerResponse.getObjects() == null || handlerResponse.getObjects().size() != 1) {
                Log.e(this.getClass().getSimpleName(), "Failed to update map object!");
                return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to update map object! Backend couldn't receive single object with given id.");
            }

            // Check if the object returned from the backend matches the object which was sent to the backend.
            MapObject returnedObject = handlerResponse.getObjects().get(0);
            if (!BasicBackendHandler.matchingMapObjects(updatedMapObject, returnedObject)) {
                return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to update map object! Returned object and sent object didn't match!");
            }

            List<MapObject> list = new ArrayList<>();
            list.add(returnedObject);
            return new HandlerResponse<>(list, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to update object, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to update object, response: "+ response);
    }

    private static boolean matchingMapObjects(MapObject obj1, MapObject obj2, Pair<String, String>... customHeaders) {
        if (!obj1.getId().equals(obj2.getId())) {
            Log.d(BasicBackendHandler.class.getSimpleName(), "matchingMapObjects id:s didn't match");
            return false;
        }

        if (!obj1.getPointLocation().equals(obj2.getPointLocation())) {
            Log.d(BasicBackendHandler.class.getSimpleName(), "matchingMapObjects locations didn't match");
            return false;
        }

        for (Map.Entry<String, String> entry : obj1.getAdditionalProperties().entrySet()) {
            if (!obj2.getAdditionalProperties().get(entry.getKey()).equals(entry.getValue())) {
                Log.d(BasicBackendHandler.class.getSimpleName(), "matchingMapObjects AdditionalProperties entry didn't match: "+ entry.getKey() + " value: "+ entry.getValue());
                return false;
            }
        }

        for (Map.Entry<String, String> entry : obj1.getMetadataProperties().entrySet()) {
            if (!obj2.getMetadataProperties().get(entry.getKey()).equals(entry.getValue())) {
                Log.d(BasicBackendHandler.class.getSimpleName(), "matchingMapObjects MetadataProperties entry didn't match: "+ entry.getKey() + " value: "+ entry.getValue());
                return false;
            }
        }
        return true;
    }

    @Override
    public HandlerResponse<String> getUsers(Pair<String, String>... customHeaders){
        String url = this.urlProvider.getBaseMessageUrl() + "users/list/";
        UrlResponse response = this.getUrl(url, RETRY_AMOUNT, customHeaders);

        // Only if the url returns code 200, we can parse the results.
        if (response != null && response.getStatus() == UrlResponse.ResponseStatus.STATUS_200) {
            List<String> users;
            try {
                users = UserParser.parseCollection(response.getContents());
            } catch (JSONException e){
                Log.e(this.getClass().getSimpleName(), "Failed to parse users from JSON! {}", e);
                return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to parse users from JSON!");
            } catch (IOException e){
                Log.e(this.getClass().getSimpleName(), "Failed to parse users from JSON! {}", e);
                return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to parse users from JSON!");
            }
            return new HandlerResponse<>(users, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to get users, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to get users, response: "+ response);
    }

    @Override
    public HandlerResponse<String> getCollections(String url, Pair<String, String>... customHeaders){
        //String url = this.baseObjectUrl;
        UrlResponse response = this.getUrl(url+"locationdata/api/", RETRY_AMOUNT, customHeaders);

        // Only if the url returns code 200, we can parse the results.
        if (response != null && response.getStatus() == UrlResponse.ResponseStatus.STATUS_200) {
            List<String> collections;
            try {
                collections = CollectionParser.parseCollection(response.getContents());
            } catch (JSONException e){
                Log.e(this.getClass().getSimpleName(), "Failed to parse collections from JSON! {}", e);
                return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to parse collections from JSON!");
            } catch (IOException e){
                Log.e(this.getClass().getSimpleName(), "Failed to parse collections from JSON! {}", e);
                return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to parse collections from JSON!");
            }
            return new HandlerResponse<>(collections, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to get collections, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to get collections, response: "+ response);
    }

    @Override
    public HandlerResponse<MessageObject> getMessages(Pair<String, String>... customHeaders) {
        String url = this.urlProvider.getBaseMessageUrl() + "messages/";
        UrlResponse response = this.getUrl(url, RETRY_AMOUNT, customHeaders);

        // Only if the url returns code 200, we can parse the results.
        if (response != null && response.getStatus() == UrlResponse.ResponseStatus.STATUS_200) {
            List<MessageObject> messages;
            try {
                messages = MessageParser.parseCollection(response.getContents());
            } catch (JSONException e){
                Log.e(this.getClass().getSimpleName(), "Failed to parse messages from JSON! {}", e);
                return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            } catch (IOException e){
                Log.e(this.getClass().getSimpleName(), "Failed to parse messages from JSON! {}", e);
                return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            }
            return new HandlerResponse<>(messages, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to get messages, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to get messages, response: "+ response);
    }

    @Override
    public HandlerResponse<MessageObject> postMessage(String receiver,
                                                      String topic,
                                                      Object message,
                                                      List<String> attachedObjectIds,
                                                      Pair<String, String>... customHeaders) {
//        String testMsg = "{\"category\": \"Streetlights\", \"recipient\": \"lbd@lbd.net\"," +
//            "\"attachements\": [{\"category\": \"Jokin\", \"aid\": \"jokin id\"}], \"topic\": \"Meeppä kuule korjaan toi valo\"," +
//            "\"message\": \"Tässä sulle tikkaat\"}";

        JsonNodeFactory factory = JsonNodeFactory.instance;
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(this.urlProvider.getBaseMessageUrl());
        urlBuilder.append("send/");

        ObjectNode objNode = factory.objectNode()
                .put("category", this.urlProvider.getBaseMessageUrl())
                .put("recipient", receiver)
                .put("topic", topic);
        addMessageToObjectNode(message, objNode);

        /*
        if (attachedObjectIds.size() > 0) {
            ArrayNode arrayNode = objNode.putArray("attachements");
            for (String object : attachedObjectIds) {
                arrayNode.addObject().put("category", dataSource).put("id", object);
            }
        }*/

        Log.d(this.getClass().getSimpleName(), "Send message: " + objNode.toString());
        Log.d(this.getClass().getSimpleName(), "Send to url: "+ urlBuilder.toString());
        UrlResponse response = this.urlReader.postJson(urlBuilder.toString(), objNode.toString(), customHeaders);

        if (response != null && response.getStatus() == UrlResponse.ResponseStatus.STATUS_200) {
            return new HandlerResponse<>(null, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to send message, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to send message, response: "+ response);
    }

    @Override
    public HandlerResponse<MessageObject> deleteMessage(String messageId, Pair<String, String>... customHeaders) {
        String url = this.urlProvider.getBaseMessageUrl() + "messages/" + /*dataSource +"/"+*/ messageId;
        UrlResponse response = this.urlReader.delete(url, customHeaders);

        Log.d(BasicBackendHandler.class.getSimpleName(), "deleteMessage from url: " + url);

        if (response != null && response.getStatus() == UrlResponse.ResponseStatus.STATUS_200) {
            return new HandlerResponse<>(null, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to delete message, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to delete message, response: "+ response);
    }

    private void addMessageToObjectNode(Object message, ObjectNode objNode, Pair<String, String>... customHeaders) {
        // TODO: JSON transformers for different message types.
        if (message instanceof String) {
            objNode.put("message", (String)message);
            return;
        }
        throw new RuntimeException("Only messages of string type are currently supported!");
    }

    /**
     * Tries to retry the reading of the URL if the returned status code is not valid.
     *
     * @param url   Url which should be read.
     * @param retries   Amount of retries before giving up.
     * @return  Returned response or null.
     */
    private UrlResponse getUrl(String url, int retries, Pair<String, String>... customHeaders) {
        UrlResponse response;
        for (int i = 0; i < retries+1; i++) {
            response = this.urlReader.get(url, customHeaders);
            if (response != null) {
                if (shouldRetry(response.getStatus())) {
                    Log.e(BasicBackendHandler.class.getSimpleName(), "Retrying request on url: "+url+", response was: "+ response);
//                    return response; // Got a response object but the returned status code was not OK.
                } else {
                    return response; // Response was valid. Return it.
                }
            }
            // If the response is null, retry.
        }
        return null;
    }

    // TODO: Which respose codes should result in retry?
    private static boolean shouldRetry(UrlResponse.ResponseStatus status) {
        return (status == UrlResponse.ResponseStatus.STATUS_500);
//                || status == URLResponse.ResponseStatus.STATUS_404);
    }
}
