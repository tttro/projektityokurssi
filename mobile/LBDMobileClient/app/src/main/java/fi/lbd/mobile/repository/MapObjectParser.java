package fi.lbd.mobile.repository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fi.lbd.mobile.mapobjects.BasicMapObject;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.PointLocation;


/**
 * Parses MapObjects from JSONObjects
 * TODO: Toimisko google-gson ?
 *
 * Created by tommi on 19.10.2014.
 *
 */
public final class MapObjectParser {
	private static final String OD_INFO_OBJECT = "OD_info";
	private static final String OD_DATA_OBJECT = "OD_data";
	private static final String OD_IDENTIFIER = "identifier_field_name";
	
	private MapObjectParser() {}
	
	public static List<MapObject> parseArrayOfObjects(JSONArray json) throws JSONException {
        List<MapObject> mapObjects = new ArrayList<MapObject>();
		for (int i = 0; i < json.length(); i++) {
			// TODO: Null check?
			mapObjects.add(MapObjectParser.parse(json.getJSONObject(i)));
		}
		return mapObjects;
	}
	
	public static MapObject parse(JSONObject jsonObj) throws JSONException {
		JSONObject odInfo = jsonObj.getJSONObject(OD_INFO_OBJECT);
		JSONObject odData = jsonObj.getJSONObject(OD_DATA_OBJECT);


		String objId = getId(odInfo, odData);
        PointLocation pointLocation = getPointLocation(odInfo, odData);

        Log.i(MapObjectParser.class.getSimpleName(), "Object id: "+ objId);
//		Iterator<?> iter = jsonObj.keys();
//		while (iter.hasNext()) {
//			String key = (String)iter.next();
//			Log.i(MapObjectParser.class.getSimpleName(), "Obj: "+ key + " val: "+ jsonObj.get(key));
//		}
		return new BasicMapObject(objId, pointLocation);
	}
	
	private static String getId(JSONObject odInfo, JSONObject odData) throws JSONException {
		String identifier = odInfo.getString(OD_IDENTIFIER);
		return odData.getString(identifier);
	}
	
	private static PointLocation getPointLocation(JSONObject odInfo, JSONObject odData) throws JSONException {
        JSONObject geometry = odData.getJSONObject("geometry");
        String coordinates = geometry.getString("coordinates");
		return PointLocation.parseFromString(coordinates);
	}


}
