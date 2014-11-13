package fi.lbd.mobile.backendhandler;

import com.fasterxml.jackson.core.JsonParseException;

import java.io.IOException;
import java.util.List;

import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.PointLocation;

/**
 * Handles the map objects. Fetches the requested objects from backend service
 *
 * Created by tommi on 19.10.2014.
 */
public class BasicBackendHandler implements BackendHandler {
    private final String baseUrl; // "http://lbdbackend.ignorelist.com/locationdata/api/"
    private final String dataSource; // "Streetlights/"

    public BasicBackendHandler(String baseUrl, String dataSource) {
        this.baseUrl = baseUrl;
        this.dataSource = dataSource;
    }

    @Override
	public List<MapObject> getObjectsNearLocation(PointLocation location, double range, boolean mini) {
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
		String contents = URLReader.get(url);

//        Log.e("MapObjectRepository-"+Thread.currentThread().getName(), "GET URL: "+ url );
//        Log.e("MapObjectRepository", "contents: "+ contents );
        List<MapObject> objects = null;
		try {
            objects = MapObjectParser.parseCollection(contents, mini);
        } catch (JsonParseException e){
            // TODO
        } catch (IOException e){
            // TODO
        }

        return objects;
	}

    @Override
    public List<MapObject> getObjectsInArea(PointLocation southWest, PointLocation northEast, boolean mini) {
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
        String contents = URLReader.get(url);
//        Log.e("MapObjectRepository-"+Thread.currentThread().getName(), "CACHE URL: "+ url );

        List<MapObject> objects = null;
        try {
            objects = MapObjectParser.parseCollection(contents, mini);
        } catch (JsonParseException e){
            // TODO
        } catch (IOException e){
            // TODO
        }

        return objects;
    }

    @Override
    public MapObject getMapObject(String id) {
        StringBuilder str = new StringBuilder();
        str.append(this.baseUrl);
        str.append(this.dataSource);
        str.append(id);

        String url = str.toString();
        String contents = URLReader.get(url);
//        Log.e("MapObjectRepository-"+Thread.currentThread().getName(), "CACHE URL: "+ url );
        MapObject mapObject = null;

        try {
            mapObject = MapObjectParser.parse(contents);
        } catch (JsonParseException e){
            // TODO
        } catch (IOException e){
            // TODO
        }

        return mapObject;
    }


}
