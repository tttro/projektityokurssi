package fi.lbd.mobile.messageobjects.events;

import fi.lbd.mobile.events.AbstractEvent;

/**
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
