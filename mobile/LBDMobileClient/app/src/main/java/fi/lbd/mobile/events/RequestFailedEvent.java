package fi.lbd.mobile.events;

import android.support.annotation.NonNull;

import fi.lbd.mobile.authorization.AuthorizedEvent;

/**
 * Returned from the backendhandler service if some request failed.
 *
 * Created by Tommi.
 */
public class RequestFailedEvent extends AbstractEvent {
    private final AbstractEvent failedEvent;
    private final String reason;

    public RequestFailedEvent(@NonNull AbstractEvent failedEvent, @NonNull String reason) {
        this.failedEvent = failedEvent;
        this.reason = reason;
    }

    public AbstractEvent getFailedEvent() {
        return failedEvent;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "Returned RequestFailedEvent. Reason: \""+ this.getReason() + "\" Original event: "+this.failedEvent.getClass();
    }
}
