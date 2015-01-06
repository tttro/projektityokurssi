package fi.lbd.mobile.backendhandler;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Ossi on 4.1.2015.
 */
public class CollectionParser {
    private CollectionParser(){};

    public static List<String> parseCollection(String json) throws IOException, JSONException {
        if (json == null) {
            throw new JSONException("Input string cannot be null!");
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        return CollectionParser.parseCollection(rootNode);
    }

    private static List<String> parseCollection(JsonNode rootNode) throws IOException, JSONException {
        List<String> collections = new ArrayList<>();

        Log.d(CollectionParser.class.getSimpleName(), "parse collection node NODE: " + collections.toString());
        Log.d(CollectionParser.class.getSimpleName(), "parse collection node NODE size: "+ collections.size());
        Iterator<JsonNode> iterator = rootNode.elements();
        while (iterator.hasNext()) {
            JsonNode node = iterator.next();
            collections.add(parseSingleCollection(node));
        }
        return collections;
    }

    private static String parseSingleCollection(JsonNode rootNode) throws IOException, JSONException {
        Log.d(UserParser.class.getSimpleName(), "parseSingleCollection node NODE: "+ rootNode);
        Log.d(UserParser.class.getSimpleName(), "parseSingleCollection node NODE size: "+ rootNode.size());

        JsonNode collectionNode = rootNode.path("name");
        check(collectionNode, "name");
        String collection = collectionNode.asText();
        return collection;
    }

    private static void check(JsonNode node, String nodeName) throws JSONException{
        if (node.isMissingNode()) {
            throw new JSONException("Json is in invalid format! "+nodeName+" node is missing.");
        }
    }
}
