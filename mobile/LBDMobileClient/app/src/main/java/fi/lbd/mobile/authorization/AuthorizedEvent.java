package fi.lbd.mobile.authorization;

import android.util.Pair;

import fi.lbd.mobile.events.AbstractEvent;

/**
 * Event which should be filled with authentication information. AuthorizedEventHandler handles
 * every event which is inherited from this event.
 *
 * Created by Tommi on 23.1.2015.
 */
public class AuthorizedEvent extends AbstractEvent {

    private Pair<String, String> idHeader;
    private Pair<String, String> tokenHeader;

    public AuthorizedEvent() {}

    public void setHeaders(String id, String token) {
        this.idHeader = new Pair<>("LBD_OAUTH_ID", id);
        this.tokenHeader = new Pair<>("LBD_LOGIN_HEADER", token);
    }

    public Pair<String, String> getTokenHeader() {
        return tokenHeader;
    }

    public Pair<String, String> getIdHeader() {
        return idHeader;
    }
}
