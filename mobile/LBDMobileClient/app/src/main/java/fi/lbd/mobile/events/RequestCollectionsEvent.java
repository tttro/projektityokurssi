package fi.lbd.mobile.events;

import fi.lbd.mobile.events.AbstractEvent;

/**
 * Created by Ossi on 4.1.2015.
 */
public class RequestCollectionsEvent extends AbstractEvent {
    private String url;

    public RequestCollectionsEvent (String url){
        this.url = url;
    }
    public String getUrl(){
        return this.url;
    }
}
