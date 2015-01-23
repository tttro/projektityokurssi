package fi.lbd.mobile.messaging.events;

import fi.lbd.mobile.authorization.AuthorizedEvent;
import fi.lbd.mobile.events.AbstractEvent;

/**
 * Returned if the message deletion succeeded.
 * Created by Tommi on 3.1.2015.
 */
public class DeleteMessageSucceededEvent extends AbstractEvent {
    private final DeleteMessageEvent originalEvent;

    public DeleteMessageSucceededEvent(DeleteMessageEvent originalEvent) {
        this.originalEvent = originalEvent;
    }

    public DeleteMessageEvent getOriginalEvent() {
        return originalEvent;
    }
}
