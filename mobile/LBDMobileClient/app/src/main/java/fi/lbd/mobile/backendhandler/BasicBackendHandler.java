package fi.lbd.mobile.backendhandler;

import android.support.annotation.NonNull;
import android.util.Log;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fi.lbd.mobile.location.PointLocation;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.messageobjects.MessageObject;

/**
 * Handles the map objects. Fetches the requested objects from backend service.
 *
 * Created by Tommi.
 */
public class BasicBackendHandler implements BackendHandler {
    protected static final int RETRY_AMOUNT = 1;
    protected final String baseObjectUrl;
    protected final String baseMessageUrl;
//    protected final String dataSource;
    protected final UrlReader urlReader;

    /**
     * Handles the map objects. Fetches the requested objects from backend service.
     *
     * @param baseObjectUrl   Base url for the object REST api.
     * @param baseMessageUrl   Base url for the message REST api.
     */
    public BasicBackendHandler(UrlReader urlReader, String baseObjectUrl, String baseMessageUrl) {
        this.urlReader = urlReader;
        this.baseObjectUrl = baseObjectUrl;
        this.baseMessageUrl = baseMessageUrl;
//        this.dataSource = dataSource;
    }

    @Override
	public HandlerResponse<MapObject> getObjectsNearLocation(String dataSource, PointLocation location, double range, boolean mini) {
        StringBuilder str = new StringBuilder();
        str.append(this.baseObjectUrl);
        str.append(dataSource);
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
        UrlResponse response = this.getUrl(url, RETRY_AMOUNT);

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
    public HandlerResponse<MapObject> getObjectsFromSearch(String dataSource, @NonNull List<String> fromFields, @NonNull String searchString,
                                                int limit, boolean mini) {
        StringBuilder str = new StringBuilder();
        str.append(this.baseObjectUrl);
        str.append(dataSource);
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
        UrlResponse response = this.urlReader.postJson(url, jsonObj.toString());

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
    public HandlerResponse<MapObject> getObjectsInArea(String dataSource, PointLocation southWest, PointLocation northEast, boolean mini) {
        StringBuilder str = new StringBuilder();
        str.append(this.baseObjectUrl);
        str.append(dataSource);
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
        UrlResponse response = this.getUrl(url, RETRY_AMOUNT);

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
    public HandlerResponse<MapObject> getMapObject(String dataSource, String id) {
        String url = this.baseObjectUrl + dataSource+ "/"+ id;
        UrlResponse response = this.getUrl(url, RETRY_AMOUNT);

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

    @Override
    public HandlerResponse<String> getUsers(){
        String url = this.baseMessageUrl + "users/list/";
        UrlResponse response = this.getUrl(url, RETRY_AMOUNT);

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
    public HandlerResponse<String> getCollections(){
        String url = this.baseObjectUrl;
        UrlResponse response = this.getUrl(url, RETRY_AMOUNT);

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
    public HandlerResponse<MessageObject> getMessages() {
        String url = this.baseMessageUrl + "messages/";
        UrlResponse response = this.getUrl(url, RETRY_AMOUNT);

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
    public HandlerResponse<MessageObject> postMessage(String dataSource,
                                                      String receiver,
                                                      String topic,
                                                      Object message,
                                                      List<String> attachedObjectIds) {
//        String testMsg = "{\"category\": \"Streetlights\", \"recipient\": \"lbd@lbd.net\"," +
//            "\"attachements\": [{\"category\": \"Jokin\", \"aid\": \"jokin id\"}], \"topic\": \"Meeppä kuule korjaan toi valo\"," +
//            "\"message\": \"Tässä sulle tikkaat\"}";

        JsonNodeFactory factory = JsonNodeFactory.instance;
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(this.baseMessageUrl);
        urlBuilder.append("send/");

        ObjectNode objNode = factory.objectNode()
                .put("category", dataSource)
                .put("recipient", receiver)
                .put("topic", topic);
        addMessageToObjectNode(message, objNode);

        // TODO: Attachments
        if (attachedObjectIds.size() > 0) {
            ArrayNode arrayNode = objNode.putArray("attachments");
            for (String object : attachedObjectIds) {
                arrayNode.addObject().put("category", dataSource).put("aid", object);
            }
        }

        Log.d(this.getClass().getSimpleName(), "Send message: " + objNode.toString());
        Log.d(this.getClass().getSimpleName(), "Send to url: "+ urlBuilder.toString());
        UrlResponse response = this.urlReader.postJson(urlBuilder.toString(), objNode.toString());

        if (response != null && response.getStatus() == UrlResponse.ResponseStatus.STATUS_200) {
            return new HandlerResponse<>(null, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to send message, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to send message, response: "+ response);
    }

    @Override
    public HandlerResponse<MessageObject> deleteMessage(String dataSource, String messageId) {
        String url = this.baseMessageUrl +dataSource +"/"+ messageId;
        UrlResponse response = this.urlReader.delete(url);

        Log.d(BasicBackendHandler.class.getSimpleName(), "deleteMessage from url: " + url);

        if (response != null && response.getStatus() == UrlResponse.ResponseStatus.STATUS_200) {
            return new HandlerResponse<>(null, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to delete message, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to delete message, response: "+ response);
    }

    private void addMessageToObjectNode(Object message, ObjectNode objNode) {
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
    private UrlResponse getUrl(String url, int retries) {
        UrlResponse response;
        for (int i = 0; i < retries+1; i++) {
            response = this.urlReader.get(url);
            if (response != null) {
                if (shouldRetry(response.getStatus())) {
                    Log.e(BasicBackendHandler.class.getSimpleName(), "Retrying request on url: "+url+", response was: "+ response);
                    return null; // Got a response object but the returned status code was not OK.
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
