package fi.lbd.mobile.messaging.events;

import fi.lbd.mobile.events.AbstractEvent;

/**
 * Returned from bus if the message send operation succeeded.
 * Created by Tommi on 3.1.2015.
 */
public class SendMessageSucceededEvent extends AbstractEvent {
    private final SendMessageEvent originalEvent;

    public SendMessageSucceededEvent(SendMessageEvent originalEvent) {
        this.originalEvent = originalEvent;
    }

    public SendMessageEvent getOriginalEvent() {
        return originalEvent;
    }
}
