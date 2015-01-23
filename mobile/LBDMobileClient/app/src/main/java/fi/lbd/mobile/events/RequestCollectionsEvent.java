package fi.lbd.mobile.events;

import fi.lbd.mobile.authorization.AuthorizedEvent;

/**
 * Created by Ossi on 4.1.2015.
 */
public class RequestCollectionsEvent extends AuthorizedEvent {
    private String url;

    public RequestCollectionsEvent (String url){
        this.url = url;
    }
    public String getUrl(){
        return this.url;
    }
}
