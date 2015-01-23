package fi.lbd.mobile.events;

import android.support.annotation.Nullable;

import java.util.List;

import fi.lbd.mobile.authorization.AuthorizedEvent;

/**
 * Created by Ossi on 4.1.2015.
 */
public class ReturnUsersEvent extends AbstractEvent {
    private List<String> users;

    public ReturnUsersEvent(@Nullable List<String> users){
        this.users = users;
    }

    public List<String> getUsers(){
        return this.users;
    }
}
