package fi.lbd.mobile.repository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fi.lbd.mobile.mapobjects.ImmutableMapObject;
import fi.lbd.mobile.mapobjects.ImmutablePointLocation;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.PointLocation;


/**
 * Parses MapObjects from JSONObjects
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
        ImmutablePointLocation pointLocation = getPointLocation(odInfo, odData);

        Log.i(MapObjectParser.class.getSimpleName(), "Object id: "+ objId);
//		Iterator<?> iter = jsonObj.keys();
//		while (iter.hasNext()) {
//			String key = (String)iter.next();
//			Log.i(MapObjectParser.class.getSimpleName(), "Obj: "+ key + " val: "+ jsonObj.get(key));
//		}
		return new ImmutableMapObject(objId, pointLocation, null);
	}
	
	private static String getId(JSONObject odInfo, JSONObject odData) throws JSONException {
		String identifier = odInfo.getString(OD_IDENTIFIER);
		return odData.getString(identifier);
	}
	
	private static ImmutablePointLocation getPointLocation(JSONObject odInfo, JSONObject odData) throws JSONException {
        JSONObject geometry = odData.getJSONObject("geometry");
        String coordinates = geometry.getString("coordinates");
		return ImmutablePointLocation.parseFromString(coordinates);
	}



    public static List<MapObject> parseCollection(String json) throws JsonParseException, IOException {
        List<MapObject> mapObjects = new ArrayList<MapObject>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);

        /*** read ***/
        JsonNode totalFeatures = rootNode.path("totalFeatures");
        System.out.println(totalFeatures.asText());

        JsonNode type = rootNode.path("type");
        System.out.println(type.asText());

        JsonNode features = rootNode.path("features");
        Iterator<JsonNode> iter = features.elements();
        while (iter.hasNext()) { // Single object loop
            JsonNode node = iter.next();

            // TODO: Joku enum millä määritellään pakolliset yms?

            String strGeometryType;
            List<Double> coordinates = new ArrayList<>();
            String strId;
            String strElemType;
            Map<String, String> additionalProperties = new HashMap<>();
            String strGeometryName;

            JsonNode geometry = node.path("geometry");
            JsonNode geometryType = geometry.findPath("type");
            strGeometryType = geometryType.asText();

            JsonNode coords = geometry.findPath("coordinates");
            Iterator<JsonNode> coordIter = coords.elements();
            while (coordIter.hasNext()) {
                JsonNode coordNode = coordIter.next();
                coordinates.add(coordNode.asDouble());
            }

            JsonNode id = node.path("id");
            strId = id.asText();

            JsonNode elemType = node.path("type");
            strElemType = elemType.asText();

            JsonNode properties = node.path("properties");
            Iterator<Map.Entry<String, JsonNode>> propertiesIter = properties.fields();
            while (propertiesIter.hasNext()) {
                Map.Entry<String, JsonNode> entry = propertiesIter.next();
                if (entry.getKey().equals("metadata")) {
                    // TODO:
                }
                additionalProperties.put(entry.getKey(), entry.getValue().asText()); // TODO: Voiko olla muitakin kuin txt propertyjä?
            }

            JsonNode geometryName = node.path("geometry_name");
            strGeometryName = geometryName.asText();


            mapObjects.add(new ImmutableMapObject(
                    strId,
                    new ImmutablePointLocation(coordinates.get(1), coordinates.get(0)),
                    additionalProperties));
        } // Single object loop


//        /*** update ***/
//        ((ObjectNode)rootNode).put("nickname", "new nickname");
//        ((ObjectNode)rootNode).put("name", "updated name");
//        ((ObjectNode)rootNode).remove("age");
//
//        mapper.writeValue(new File("c:\\user.json"), rootNode);

        // TODO: Factory ois nopeempi mut työläämpi ja virhealttiimi + tuleeko mitään ajallista hyötyä tässä?
//        JsonFactory jfactory = new JsonFactory();
//        JsonParser jParser = jfactory.createParser(json);
//
//        // LOOP UNTIL WE READ END OF JSON DATA, INDICATED BY }
//        while (!jParser.isClosed()) {
//            String fieldname = jParser.getCurrentName();
//            if ("totalFeatures".equals(fieldname)) {
//                jParser.nextToken(); // To value
//                System.out.println(jParser.getText());
//            }
//            if ("type".equals(fieldname)) {
//                jParser.nextToken(); // To value
//                System.out.println(jParser.getText());
//            }
//            if ("features".equals(fieldname)) {
//                jParser.nextToken();
//                // iterate through the array until token equal to "]"
//                while (jParser.nextToken() != JsonToken.END_ARRAY) {
//                    // output the array data
//                    System.out.println(jParser.getText());
//                }
//            }
//            jParser.nextToken();
//        }
//        jParser.close();
        return mapObjects;
    }
}
