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
public final class UserParser {
    private UserParser(){}

    public static List<String> parseCollection(String json) throws IOException, JSONException {
        if (json == null) {
            throw new JSONException("Input string cannot be null!");
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        return UserParser.parseCollection(rootNode);
    }

    private static List<String> parseCollection(JsonNode rootNode) throws IOException, JSONException {
        List<String> users = new ArrayList<>();

        Log.d(UserParser.class.getSimpleName(), "parse collection node NODE: " + users.toString());
        Log.d(UserParser.class.getSimpleName(), "parse collection node NODE size: "+ users.size());
        Iterator<JsonNode> iterator = rootNode.elements();
        while (iterator.hasNext()) {
            JsonNode node = iterator.next();
            users.add(parseUser(node));
        }

        return users;
    }

    private static String parseUser(JsonNode rootNode) throws IOException, JSONException {
        Log.d(UserParser.class.getSimpleName(), "parseUser node NODE: "+ rootNode);
        Log.d(UserParser.class.getSimpleName(), "parseUser node NODE size: "+ rootNode.size());

        JsonNode emailNode = rootNode.path("email");
        check(emailNode, "email");
        String email = emailNode.asText();
        return email;
    }

    private static void check(JsonNode node, String nodeName) throws JSONException{
        if (node.isMissingNode()) {
            throw new JSONException("Json is in invalid format! "+nodeName+" node is missing.");
        }
    }
}
