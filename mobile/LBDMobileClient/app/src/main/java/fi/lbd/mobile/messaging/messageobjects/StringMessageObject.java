package fi.lbd.mobile.messaging.messageobjects;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 *
 * Concrete class representing a MessageObject that has a simple String as a message.
 *
 * Created by Ossi on 29.12.2014.
 */
public class StringMessageObject extends MessageObject<String> {

    private final String message;

    public StringMessageObject(String id, long timestamp, @NonNull String receiver, @NonNull String sender,
                         @NonNull String topic, @NonNull boolean isRead, @NonNull String message){
        super(id, timestamp, receiver, sender, topic, isRead);
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }

    @Override
    public String toString(){
        return "StringMessageObject: message("+ this.message +") parent: "+ super.toString();
    }

    @Override
    public boolean equals(Object object){
        if(object == null){
            return false;
        }
        if(!(object instanceof StringMessageObject)){
            return false;
        }
        if(object == this){
            return true;
        }

        StringMessageObject newObject = (StringMessageObject)object;
        if(newObject.getReceiver().equals(this.getReceiver())
                && newObject.getId().equals(this.getId())
                && newObject.getMessage().equals(this.getMessage())
                && newObject.getSender().equals(this.getSender())
                && newObject.getTopic().equals(this.getTopic())
                && newObject.getTimestamp() == this.getTimestamp()){
            return true;
        }
        else {
            return false;
        }
    }
}
