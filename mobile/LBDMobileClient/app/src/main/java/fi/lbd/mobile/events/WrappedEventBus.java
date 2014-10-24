package fi.lbd.mobile.events;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * Forces the events to the main thread and set a required abstract type for the events
 * http://stackoverflow.com/questions/15431768/how-to-send-event-from-service-to-activity-with-otto-event-bus
 * Created by tommi on 22.10.2014.
 */
public class WrappedEventBus {
    private final Bus bus = new Bus();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public void post(final AbstractEvent event) {
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

    public void register(Object object) {
        this.bus.register(object);
    }

    public void unregister(java.lang.Object object) {
        this.bus.unregister(object);
    }
}
