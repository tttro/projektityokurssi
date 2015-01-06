package fi.lbd.mobile.backendhandler;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fi.lbd.mobile.messaging.messageobjects.MessageObject;
import fi.lbd.mobile.messaging.messageobjects.StringMessageObject;

/**
 * Created by Tommi on 20.12.2014.
 */
public class MessageParser {
    public static List<MessageObject> parseCollection(String json) throws IOException, JSONException {
        if (json == null) {
            throw new JSONException("Input string cannot be null!");
        }

//        Log.d(MessageParser.class.getSimpleName(), "parse message json: "+ json);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        return MessageParser.parseCollection(rootNode);
    }

    private static List<MessageObject> parseCollection(JsonNode rootNode) throws IOException, JSONException {
        List<MessageObject> messages = new ArrayList<>();

        Log.d(MessageParser.class.getSimpleName(), "parse collection node NODE: "+ messages.toString());
        Log.d(MessageParser.class.getSimpleName(), "parse collection node NODE size: "+ messages.size());
        Iterator<JsonNode> iter = rootNode.elements();
        while (iter.hasNext()) {
            JsonNode node = iter.next();
            messages.add(parseMessage(node));
        }

        return messages;
    }

    private static MessageObject parseMessage(JsonNode rootNode) throws IOException, JSONException {
        //List<MessageObject> messages = new ArrayList<>();

        Log.d(MessageParser.class.getSimpleName(), "parseMessage node NODE: "+ rootNode);
        Log.d(MessageParser.class.getSimpleName(), "parseMessage node NODE size: "+ rootNode.size());

        JsonNode categoryNode = rootNode.path("category");
//        check(categoryNode, "Category");
        String category = categoryNode.asText();

        JsonNode senderNode = rootNode.path("sender");
        check(senderNode, "Sender");
        String sender = senderNode.asText();

        JsonNode midNode = rootNode.path("mid");
        check(midNode, "MID");
        String messageId = midNode.asText();

        JsonNode topicNode = rootNode.path("topic");
        check(topicNode, "Topic");
        String topic = topicNode.asText();

        JsonNode attachmentsNode = rootNode.path("attachments");
//        check(attachmentsNode, "Attachments");
        if (!attachmentsNode.isMissingNode()) {
            Log.i(MessageParser.class.getSimpleName(), "HAS ATTACHMENTS: "+ attachmentsNode.toString());
            Log.i(MessageParser.class.getSimpleName(), "HAS ATTACHMENTS, IS ARRAY: "+ attachmentsNode.isArray());
            for (Iterator<JsonNode> iter = attachmentsNode.elements(); iter.hasNext(); ) {
                JsonNode node = iter.next();
                Log.i(MessageParser.class.getSimpleName(), "attachment Node: "+ node.toString());
            }
        }

        JsonNode messageNode = rootNode.path("message");
        check(messageNode, "Message");
        String message = messageNode.asText();

        JsonNode recipientNode = rootNode.path("recipient");
        check(recipientNode, "Recipient");
        String recipient = recipientNode.asText();

        return new StringMessageObject(messageId, recipient, sender, topic, false, message);
    }

    private static void check(JsonNode node, String nodeName) throws JSONException{
        if (node.isMissingNode()) {
            throw new JSONException("Json is in invalid format! "+nodeName+" node is missing.");
        }
    }
}
