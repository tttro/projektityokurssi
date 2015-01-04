package fi.lbd.mobile.messageobjects.events;

import fi.lbd.mobile.events.AbstractEvent;

/**
 * Created by Ossi on 4.1.2015.
 */
public class SelectReceiverEvent  extends AbstractEvent {
    private String receiver;

    public SelectReceiverEvent (String receiver){
        this.receiver = receiver;
    }

    public String getReceiver(){
        return this.receiver;
    }
}
