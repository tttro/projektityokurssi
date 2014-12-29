package fi.lbd.mobile.messageobjects;

import android.support.annotation.NonNull;

/**
 *
 *  Abstract class for MessageObjects.
 *
 * Created by Ossi on 29.12.2014.
 */
public abstract class MessageObject {
    private final String id;
    private final String receiver;
    private final String sender;
    private final String topic;
    private boolean isRead;

    public MessageObject(String id, @NonNull String receiver, @NonNull String sender,
                         @NonNull String topic, @NonNull boolean isRead){
        this.id = id;
        this.receiver = receiver;
        this.sender = sender;
        this.topic = topic;
        this.isRead = isRead;
    }

    public String getId(){
        return this.id;
    }

    public String getReceiver(){
        return this.receiver;
    }

    public String getSender(){
        return this.sender;
    }

    public String getTopic(){
        return this.topic;
    }

    public boolean isRead(){
        return this.isRead;
    }
}
