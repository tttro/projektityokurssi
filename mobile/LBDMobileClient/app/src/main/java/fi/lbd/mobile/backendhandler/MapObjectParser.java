package fi.lbd.mobile.backendhandler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fi.lbd.mobile.mapobjects.ImmutableMapObject;
import fi.lbd.mobile.mapobjects.ImmutablePointLocation;
import fi.lbd.mobile.mapobjects.MapObject;


/**
 * Parses MapObjects from JSONObjects
 *
 * Created by tommi on 19.10.2014.
 *
 */
public final class MapObjectParser {
	
	private MapObjectParser() {}


    // TODO: Löytyykö vaaditut elementit
    // TODO: Onko input null?
    // TODO: Onko feature setin lukumäärä sama kuin parsittujen?
    public static List<MapObject> parseCollection(String json, boolean minimized) throws JsonParseException, IOException {
        List<MapObject> mapObjects = new ArrayList<MapObject>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);

        JsonNode totalFeatures = rootNode.path("totalFeatures");
//        System.out.println(totalFeatures.asText());

        JsonNode type = rootNode.path("type");
//        System.out.println(type.asText());

        JsonNode features = rootNode.path("features");
        Iterator<JsonNode> iter = features.elements();
        while (iter.hasNext()) { // Single object loop
            JsonNode node = iter.next();
            mapObjects.add(parseImmutableObjectFromNode(node, minimized));
        } // Single object loop


        return mapObjects;
    }

    public static MapObject parse(String json) throws JsonParseException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        return parseImmutableObjectFromNode(rootNode, false);
    }

    // TODO: Fiksumpi tapa kaivaa kuin suoraan kertomalla stringillä mitä haetaan?
    private static MapObject parseImmutableObjectFromNode(JsonNode node, boolean minimized) {
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

        return new ImmutableMapObject(
                minimized,
                strId,
                new ImmutablePointLocation(coordinates.get(1), coordinates.get(0)),
                additionalProperties);
    }
}
