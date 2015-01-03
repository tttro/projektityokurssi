package fi.lbd.mobile.messages;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fi.lbd.mobile.backendhandler.MapObjectParser;
import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Created by Tommi on 20.12.2014.
 */
public class MessageParser {
    public static List<Message> parseCollection(String json) throws IOException, JSONException {
        if (json == null) {
            throw new JSONException("Input string cannot be null!");
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        return MessageParser.parseCollection(rootNode);
    }

    private static List<Message> parseCollection(JsonNode rootNode) throws IOException, JSONException {
        List<Message> messages = new ArrayList<>();
//
//        JsonNode totalFeatures = rootNode.path("totalFeatures");
//        int intTotalFeatures = totalFeatures.asInt();
//        JsonNode type = rootNode.path("type");
//
//        JsonNode features = rootNode.path("features");
//        check(features, "Features");
//
//        Iterator<JsonNode> iter = features.elements();
//        while (iter.hasNext()) {
//            JsonNode node = iter.next();
//            messages.add(parseImmutableObjectFromNode(node, minimized));
//        }
//
//        if (intTotalFeatures != mapObjects.size()) {
//            Log.e(MapObjectParser.class.getSimpleName(),
//                    "Amount of items in json feature set differs from the amount of parsed items." +
//                            " Feature count: " + intTotalFeatures +
//                            " Parsed count" + mapObjects.size());
//        }
        return messages;
    }

    private static void check(JsonNode node, String nodeName) throws JSONException{
        if (node.isMissingNode()) {
            throw new JSONException("Json is in invalid format! "+nodeName+" node is missing.");
        }
    }
}
