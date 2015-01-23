package fi.lbd.mobile.events;

import android.support.annotation.Nullable;

import java.util.List;

import fi.lbd.mobile.authorization.AuthorizedEvent;

/**
 * Created by Ossi on 4.1.2015.
 */
public class ReturnCollectionsEvent extends AbstractEvent {
    private List<String> collections;

    public ReturnCollectionsEvent(@Nullable List<String> collections){
        this.collections = collections;
    }

    public List<String> getCollections(){
        return this.collections;
    }
}
