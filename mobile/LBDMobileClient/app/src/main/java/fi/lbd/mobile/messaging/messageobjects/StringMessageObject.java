package fi.lbd.mobile.messaging.messageobjects;

import android.support.annotation.NonNull;

/**
 *
 * Concrete class representing a MessageObject that has a simple String as a message.
 *
 * Created by Ossi on 29.12.2014.
 */
public class StringMessageObject extends MessageObject<String> {

    private final String message;

    public StringMessageObject(String id, @NonNull String receiver, @NonNull String sender,
                         @NonNull String topic, @NonNull boolean isRead, @NonNull String message){
        super(id, receiver, sender, topic, isRead);
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }

    @Override
    public String toString(){
        return "StringMessageObject: message("+ this.message +") parent: "+ super.toString();
    }
}
