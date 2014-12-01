package fi.lbd.mobile.backendhandler;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fi.lbd.mobile.mapobjects.ImmutableMapObject;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.mapobjects.MapObject;


/**
 * Parses MapObjects from JSONObjects
 *
 * Created by Tommi.
 *
 */
public final class MapObjectParser {
	private MapObjectParser() {}

    /**
     * Parses a string of json which contains multiple elements.
     *
     * @param json  String to be parsed.
     * @param minimized Is the text in minimized format.
     * @return  List of parsed objects.
     * @throws IOException
     * @throws JSONException
     */
    public static List<MapObject> parseCollection(String json, boolean minimized) throws IOException, JSONException {
        if (json == null) {
            throw new JSONException("Input string cannot be null!");
        }
        List<MapObject> mapObjects = new ArrayList<MapObject>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);

        JsonNode totalFeatures = rootNode.path("totalFeatures");
        int intTotalFeatures = totalFeatures.asInt();
        JsonNode type = rootNode.path("type");

        JsonNode features = rootNode.path("features");
        check(features, "Features");

        Iterator<JsonNode> iter = features.elements();
        while (iter.hasNext()) {
            JsonNode node = iter.next();
            mapObjects.add(parseImmutableObjectFromNode(node, minimized));
        }

        if (intTotalFeatures != mapObjects.size()) {
            Log.e(MapObjectParser.class.getSimpleName(),
                    "Amount of items in json feature set differs from the amount of parsed items."+
                    " Feature count: "+ intTotalFeatures +
                    " Parsed count"+ mapObjects.size());
        }
        return mapObjects;
    }

    /**
     * Parses a string of json which contains a single elements.
     *
     * @param json  String to be parsed.
     * @return  Single parsed object.
     * @throws IOException
     * @throws JSONException
     */
    public static MapObject parse(String json) throws IOException, JSONException {
        if (json == null) {
            throw new JSONException("Input string cannot be null!");
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        return parseImmutableObjectFromNode(rootNode, false);
    }

    /**
     * Parses a map object from the given node.
     *
     * @param node  Node which is parsed.
     * @param minimized Is the node in minimized format.
     * @return  A parsed map object.
     * @throws JSONException
     */
    private static MapObject parseImmutableObjectFromNode(JsonNode node, boolean minimized) throws JSONException {
        // TODO: Fiksumpi tapa kaivaa kuin suoraan kertomalla stringillä mitä haetaan? + mitä kaikkea oltava jos mini?
        String strGeometryType;
        List<Double> coordinates = new ArrayList<>();
        String strId;
        String strElemType;
        Map<String, String> additionalProperties = new HashMap<>();
        Map<String, String> metadataProperties = new HashMap<>();
        String strGeometryName;

        JsonNode geometry = node.path("geometry");
        check(geometry, "Geometry");

        JsonNode geometryType = geometry.findPath("type");
        strGeometryType = geometryType.asText();

        JsonNode coords = geometry.findPath("coordinates");
        check(coords, "Coordinates");
        Iterator<JsonNode> coordIter = coords.elements();
        while (coordIter.hasNext()) {
            JsonNode coordNode = coordIter.next();
            coordinates.add(coordNode.asDouble());
        }

        JsonNode id = node.path("id");
        check(id, "Id");
        strId = id.asText();

        JsonNode elemType = node.path("type");
        check(elemType, "Element type");
        strElemType = elemType.asText();

        JsonNode properties = node.path("properties");
//        check(properties, "Properties");

        Iterator<Map.Entry<String, JsonNode>> propertiesIter = properties.fields();
        while (propertiesIter.hasNext()) {
            Map.Entry<String, JsonNode> entry = propertiesIter.next();
            if (entry.getKey().equals("metadata")) {
                JsonNode metaNode = entry.getValue();
                Iterator<Map.Entry<String, JsonNode>> metaNodeIter = metaNode.fields();
                while (metaNodeIter.hasNext()) {
                    Map.Entry<String, JsonNode> metaNodeEntry = metaNodeIter.next();
                    metadataProperties.put(metaNodeEntry.getKey(), metaNodeEntry.getValue().asText()); // TODO: Voiko olla muitakin kuin txt propertyjä?
                }
            } else {
                additionalProperties.put(entry.getKey(), entry.getValue().asText()); // TODO: Voiko olla muitakin kuin txt propertyjä?
            }
        }

        JsonNode geometryName = node.path("geometry_name");
        strGeometryName = geometryName.asText();

        // TODO: Other geometries
        if (coordinates.size() != 2) {
            throw new JSONException("Only point location supported. Coordinates size: "+ coordinates.size());
        }
        return new ImmutableMapObject(
                minimized,
                strId,
                new ImmutablePointLocation(coordinates.get(1), coordinates.get(0)),
                additionalProperties,
                metadataProperties);
    }

    private static void check(JsonNode node, String nodeName) throws JSONException{
        if (node.isMissingNode()) {
            throw new JSONException("Json is in invalid format! "+nodeName+" node is missing.");
        }
    }
}
