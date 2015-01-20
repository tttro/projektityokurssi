package fi.lbd.mobile.backendhandler;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.messaging.messageobjects.MessageObject;
import fi.lbd.mobile.messaging.messageobjects.StringMessageObject;

/**
 * Parses message JSON into message objects.
 * TODO: Parse attachments
 *
 * Created by Tommi on 20.12.2014.
 */
public class MessageParser {
    public static List<MessageObject> parseCollection(String json) throws IOException, JSONException {
        if (json == null) {
            throw new JSONException("Input string cannot be null!");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        return MessageParser.parseCollection(rootNode);
    }

    private static List<MessageObject> parseCollection(JsonNode rootNode) throws IOException, JSONException {
        List<MessageObject> messages = new ArrayList<>();

        JsonNode totalMessages = rootNode.path("totalMessages");
        int intTotalMessages = totalMessages.asInt();
        JsonNode type = rootNode.path("type");

        JsonNode messagesNode = rootNode.path("messages");
        check(messagesNode, "Messages");

        Iterator<JsonNode> iter = messagesNode.elements();
        while (iter.hasNext()) {
            JsonNode node = iter.next();
            messages.add(parseMessage(node));
        }

        if (intTotalMessages != messages.size()) {
            Log.e(MessageParser.class.getSimpleName(),
                    "Amount of items in json feature set differs from the amount of parsed items."+
                            " Feature count: "+ intTotalMessages +
                            " Parsed count"+ messages.size());
        }

        return messages;
    }

    private static MessageObject parseMessage(JsonNode rootNode) throws IOException, JSONException {
        Log.d(MessageParser.class.getSimpleName(), "parse message node json: "+ rootNode);
        JsonNode categoryNode = rootNode.path("category");
        String category = categoryNode.asText();

        JsonNode senderNode = rootNode.path("sender");
        check(senderNode, "Sender");
        String sender = senderNode.asText();

        JsonNode midNode = rootNode.path("id");
        check(midNode, "ID");
        String messageId = midNode.asText();

        JsonNode topicNode = rootNode.path("topic");
        check(topicNode, "Topic");
        String topic = topicNode.asText();

        JsonNode timestampNode = rootNode.path("timestamp");
        check(topicNode, "Timestamp");
        long timestamp = timestampNode.asLong(); // In ms
        /*
        JsonNode attachmentsNode = rootNode.path("attachements");
        if (!attachmentsNode.isMissingNode()) {
            Log.i(MessageParser.class.getSimpleName(), "HAS ATTACHMENTS: "+ attachmentsNode.toString());
            Log.i(MessageParser.class.getSimpleName(), "HAS ATTACHMENTS, IS ARRAY: "+ attachmentsNode.isArray());
            for (Iterator<JsonNode> iter = attachmentsNode.elements(); iter.hasNext(); ) {
                JsonNode node = iter.next();
                Log.i(MessageParser.class.getSimpleName(), "attachment Node: "+ node.toString());
                // TODO: Parse attachments
            }
        }
        */

        JsonNode messageNode = rootNode.path("message");
        check(messageNode, "Message");
        String message = messageNode.asText();

        JsonNode recipientNode = rootNode.path("recipient");
        check(recipientNode, "Recipient");
        String recipient = recipientNode.asText();

        return new StringMessageObject(messageId, timestamp, recipient, sender, topic, false, message);
    }

    private static void check(JsonNode node, String nodeName) throws JSONException{
        if (node.isMissingNode()) {
            throw new JSONException("Json is in invalid format! "+nodeName+" node is missing.");
        }
    }
}
