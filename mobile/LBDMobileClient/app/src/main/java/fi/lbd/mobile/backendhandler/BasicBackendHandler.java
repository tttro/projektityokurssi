package fi.lbd.mobile.backendhandler;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

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
    protected final String baseUrl;
    protected final String dataSource;
    protected final UrlReader urlReader;

    /**
     * Handles the map objects. Fetches the requested objects from backend service.
     *
     * @param baseUrl   Base url for the REST api.
     * @param dataSource    Data source which should be loaded. For example "Streetlights/"
     */
    public BasicBackendHandler(UrlReader urlReader, String baseUrl, String dataSource) {
        this.urlReader = urlReader;
        this.baseUrl = baseUrl;
        this.dataSource = dataSource;
    }

    @Override
	public HandlerResponse<MapObject> getObjectsNearLocation(PointLocation location, double range, boolean mini) {
        StringBuilder str = new StringBuilder();
        str.append(this.baseUrl);
        str.append(this.dataSource);
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
                return new HandlerResponse(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            } catch (IOException e) {
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            }
            return new HandlerResponse(objects, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to get objects near location, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to get objects near location, response: "+ response);
	}

    @Override
    public HandlerResponse<MapObject> getObjectsFromSearch(@NonNull List<String> fromFields, @NonNull String searchString,
                                                int limit, boolean mini) {
        StringBuilder str = new StringBuilder();
        str.append(this.baseUrl);
        str.append(this.dataSource);
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

        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("from", fields.toString());
            jsonObj.put("search", searchString);
            jsonObj.put("limit", limit);
        } catch (JSONException e) {
            Log.e(BasicUrlReader.class.getSimpleName(), "ERROR. {}", e);

        }
        UrlResponse response = this.urlReader.postJson(url, jsonObj.toString());

        // Only if the url returns code 200, we can parse the results.
        if (response != null && response.getStatus() == UrlResponse.ResponseStatus.STATUS_200) {
            List<MapObject> objects;
            try {
                objects = MapObjectParser.parseSearchResult(response.getContents(), mini);
            } catch (JSONException e) {
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            } catch (IOException e) {
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            }
            return new HandlerResponse(objects, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to get objects from search, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to get objects from search, response: "+ response);
    }

    @Override
    public HandlerResponse<MapObject> getObjectsInArea(PointLocation southWest, PointLocation northEast, boolean mini) {
        StringBuilder str = new StringBuilder();
        str.append(this.baseUrl);
        str.append(this.dataSource);
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
                return new HandlerResponse(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            } catch (IOException e) {
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            }
            return new HandlerResponse(objects, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to get objects in area, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to get objects in area, response: "+ response);
    }

    @Override
    public HandlerResponse<MapObject> getMapObject(String id) {
        StringBuilder str = new StringBuilder();
        str.append(this.baseUrl);
        str.append(this.dataSource);
        str.append(id);

        String url = str.toString();
        UrlResponse response = this.getUrl(url, RETRY_AMOUNT);

        // Only if the url returns code 200, we can parse the results.
        if (response != null && response.getStatus() == UrlResponse.ResponseStatus.STATUS_200) {
            MapObject mapObject;
            try {
                mapObject = MapObjectParser.parse(response.getContents());
            } catch (JSONException e){
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            } catch (IOException e){
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            }
            List<MapObject> list = new ArrayList<>();
            list.add(mapObject);
            return new HandlerResponse(list, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to get object, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to get object, response: "+ response);
    }


    @Override
    public HandlerResponse<MessageObject> getMessages() {
        StringBuilder str = new StringBuilder();
        str.append("https://lbdbackend.ignorelist.com/messagedata/api/messages/");
//        str.append(userId);
//        str.append("messages/"+userId);
//        str.append(this.dataSource);

        String url = str.toString();
        UrlResponse response = this.getUrl(url, RETRY_AMOUNT);

        Log.i("ASDASDASDASDASASD", "URL: "+ url + " response: "+ response);


        // Only if the url returns code 200, we can parse the results.
        if (response != null && response.getStatus() == UrlResponse.ResponseStatus.STATUS_200) {
            Log.i("ASDASDASDASDASASD", "Contents: "+ response.getContents());
            List<MessageObject> messages;
            try {
                messages = MessageParser.parseCollection(response.getContents());
            } catch (JSONException e){
                Log.e(this.getClass().getSimpleName(), "Failed to parse messages from JSON! {}", e);
                return new HandlerResponse(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            } catch (IOException e){
                Log.e(this.getClass().getSimpleName(), "Failed to parse messages from JSON! {}", e);
                return new HandlerResponse(null, HandlerResponse.Status.Failed, "Failed to parse messages from JSON!");
            }
            return new HandlerResponse<>(messages, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to get messages, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to get messages, response: "+ response);
    }

    @Override
    public HandlerResponse<MessageObject> postMessage() {
        StringBuilder str = new StringBuilder();
        str.append("http://lbdbackend.ignorelist.com/messagedata/api/send/");

        String testMsg = "{\"category\": \"Streetlights\", \"recipient\": \"lbd@lbd.net\"," +
            "\"attachements\": [{\"category\": \"Jokin\", \"aid\": \"jokin id\"}], \"topic\": \"Meeppä kuule korjaan toi valo\"," +
            "\"message\": \"Tässä sulle tikkaat\"}";


        UrlResponse response = this.urlReader.postJson(str.toString(), testMsg);

        // Only if the url returns code 200, we can parse the results.
        if (response != null && response.getStatus() == UrlResponse.ResponseStatus.STATUS_200) {
//            List<MapObject> objects;
//            try {
//                objects = MapObjectParser.parseSearchResult(response.getContents(), mini);
//            } catch (JSONException e) {
//                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
//                return new HandlerResponse(null, HandlerResponse.Status.Failed);
//            } catch (IOException e) {
//                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
//                return new HandlerResponse(null, HandlerResponse.Status.Failed);
//            }
            return new HandlerResponse(null, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to get objects from send message, response: "+ response);
        return new HandlerResponse<>(null, HandlerResponse.Status.Failed, "Failed to get objects from send message, response: "+ response);
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
                    continue; // Got a response object but the returned status code was not OK.
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
