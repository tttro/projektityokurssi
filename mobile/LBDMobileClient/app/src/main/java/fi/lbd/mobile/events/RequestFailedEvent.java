package fi.lbd.mobile.events;

import android.support.annotation.NonNull;

import com.google.android.gms.internal.id;

/**
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
}
