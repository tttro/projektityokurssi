package fi.lbd.mobile.events;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.squareup.otto.Bus;

import fi.lbd.mobile.authorization.AuthorizedEvent;
import fi.lbd.mobile.authorization.AuthorizedEventHandler;

/**
 * Forces the events to the main thread and set a required abstract type for the events
 * http://stackoverflow.com/questions/15431768/how-to-send-event-from-service-to-activity-with-otto-event-bus
 * Created by tommi on 22.10.2014.
 */
public class WrappedEventBus {
    private boolean isTestMode = false;
    private final Bus bus = new Bus();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public void post(final AbstractEvent event) {
        this.post(event, true);
    }

    public void setTestMode(boolean isTestMode) {
        this.isTestMode = isTestMode;
    }

    public void post(final AbstractEvent event, boolean checkAuthorized) {
        if (checkAuthorized && (event instanceof AuthorizedEvent) && !this.isTestMode) {
            Log.v(this.getClass().getSimpleName(), "Bus(" + this + ") got authorized event: " + event);
            AuthorizedEvent authorizedEvent = (AuthorizedEvent)event;
            AuthorizedEventHandler.process(authorizedEvent);
        } else {
            Log.v(this.getClass().getSimpleName(), "Bus(" + this + ") event: " + event);
            if (Looper.myLooper() == Looper.getMainLooper()) {
                this.bus.post(event);
            } else {
                this.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        WrappedEventBus.this.bus.post(event);
                    }
                });
            }
        }
    }

    public void register(Object object) {
        Log.v(this.getClass().getSimpleName(), "Bus("+this+") register: "+ object);
        this.bus.register(object);
    }

    public void unregister(java.lang.Object object) {
        Log.v(this.getClass().getSimpleName(), "Bus("+this+") unregister: "+ object);
        this.bus.unregister(object);
    }
}
