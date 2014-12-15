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

/**
 * Handles the map objects. Fetches the requested objects from backend service.
 *
 * Created by Tommi.
 */
public class BasicBackendHandler implements BackendHandler {
    private static final int RETRY_AMOUNT = 1;
    private final String baseUrl;
    private final String dataSource;

    /**
     * Handles the map objects. Fetches the requested objects from backend service.
     *
     * @param baseUrl   Base url for the REST api.
     * @param dataSource    Data source which should be loaded. For example "Streetlights/"
     */
    public BasicBackendHandler(String baseUrl, String dataSource) {
        this.baseUrl = baseUrl;
        this.dataSource = dataSource;
    }

    @Override
	public HandlerResponse getObjectsNearLocation(PointLocation location, double range, boolean mini) {
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
        URLResponse response = BasicBackendHandler.getUrl(url, RETRY_AMOUNT);

        // Only if the url returns code 200, we can parse the results.
        if (response != null && response.getStatus() == URLResponse.ResponseStatus.STATUS_200) {
            List<MapObject> objects = null;
            try {
                objects = MapObjectParser.parseCollection(response.getContents(), mini);
            } catch (JSONException e) {
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse(null, HandlerResponse.Status.Failed);
            } catch (IOException e) {
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse(null, HandlerResponse.Status.Failed);
            }
            return new HandlerResponse(objects, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to get objects near location, response: "+ response);
        return new HandlerResponse(null, HandlerResponse.Status.Failed);
	}

    @Override
    public HandlerResponse getObjectsFromSearch(@NonNull List<String> fromFields, @NonNull String searchString,
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
            Log.e(URLReader.class.getSimpleName(), "ERROR. {}", e);

        }
        URLResponse response = URLReader.postJson(url, jsonObj.toString());

        // Only if the url returns code 200, we can parse the results.
        if (response != null && response.getStatus() == URLResponse.ResponseStatus.STATUS_200) {
            List<MapObject> objects = null;
            try {
                objects = MapObjectParser.parseSearchResult(response.getContents(), mini);
            } catch (JSONException e) {
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse(null, HandlerResponse.Status.Failed);
            } catch (IOException e) {
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse(null, HandlerResponse.Status.Failed);
            }
            return new HandlerResponse(objects, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to get objects from search, response: "+ response);
        return new HandlerResponse(null, HandlerResponse.Status.Failed);
    }

    @Override
    public HandlerResponse getObjectsInArea(PointLocation southWest, PointLocation northEast, boolean mini) {
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
        URLResponse response = BasicBackendHandler.getUrl(url, RETRY_AMOUNT);

        // Only if the url returns code 200, we can parse the results.
        if (response != null && response.getStatus() == URLResponse.ResponseStatus.STATUS_200) {
            List<MapObject> objects = null;
            try {
                objects = MapObjectParser.parseCollection(response.getContents(), mini);
            } catch (JSONException e) {
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse(null, HandlerResponse.Status.Failed);
            } catch (IOException e) {
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse(null, HandlerResponse.Status.Failed);
            }
            return new HandlerResponse(objects, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to get objects in area, response: "+ response);
        return new HandlerResponse(null, HandlerResponse.Status.Failed);
    }

    @Override
    public HandlerResponse getMapObject(String id) {
        StringBuilder str = new StringBuilder();
        str.append(this.baseUrl);
        str.append(this.dataSource);
        str.append(id);

        String url = str.toString();
        URLResponse response = BasicBackendHandler.getUrl(url, RETRY_AMOUNT);

        // Only if the url returns code 200, we can parse the results.
        if (response != null && response.getStatus() == URLResponse.ResponseStatus.STATUS_200) {
            MapObject mapObject = null;
            try {
                mapObject = MapObjectParser.parse(response.getContents());
            } catch (JSONException e){
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse(null, HandlerResponse.Status.Failed);
            } catch (IOException e){
                Log.e(this.getClass().getSimpleName(), "Failed to parse map objects from JSON! {}", e);
                return new HandlerResponse(null, HandlerResponse.Status.Failed);
            }
            List<MapObject> list = new ArrayList<>();
            list.add(mapObject);
            return new HandlerResponse(list, HandlerResponse.Status.Succeeded);
        }
        Log.e(BasicBackendHandler.class.getSimpleName(), "Failed to get object, response: "+ response);
        return new HandlerResponse(null, HandlerResponse.Status.Failed);
    }

    /**
     * Tries to retry the reading of the URL if the returned status code is not valid.
     *
     * @param url   Url which should be read.
     * @param retries   Amount of retries before giving up.
     * @return  Returned response or null.
     */
    private static URLResponse getUrl(String url, int retries) {
        URLResponse response = null;
        for (int i = 0; i < retries+1; i++) {
            response = URLReader.get(url);
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
    private static boolean shouldRetry(URLResponse.ResponseStatus status) {
        return (status == URLResponse.ResponseStatus.STATUS_500
                || status == URLResponse.ResponseStatus.STATUS_404);
    }
}
