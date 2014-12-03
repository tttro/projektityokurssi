package fi.lbd.mobile.backendhandler;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.squareup.otto.Subscribe;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import fi.lbd.mobile.R;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.CacheObjectsInAreaEvent;
import fi.lbd.mobile.events.RequestFailedEvent;
import fi.lbd.mobile.events.RequestMapObjectEvent;
import fi.lbd.mobile.events.RequestNearObjectsEvent;
import fi.lbd.mobile.events.RequestObjectsInAreaEvent;
import fi.lbd.mobile.events.ReturnMapObjectEvent;
import fi.lbd.mobile.events.ReturnNearObjectsEvent;
import fi.lbd.mobile.events.ReturnObjectsInAreaEvent;
import fi.lbd.mobile.events.ReturnSearchResultEvent;
import fi.lbd.mobile.events.SearchObjectsEvent;
import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Service which interacts with the backend handler. Communication is done through OTTO-bus.
 *
 * Created by Tommi.
 */
public class BackendHandlerService extends Service {
    private static final long MAX_CACHE_TIME = 1000 * 60 * 1; // 1 Min

    // Amount of threads in the executor pool
    private static final int EXECUTING_THREADS = 4;
    private static final int THREAD_TIMEOUT = 10;

    // Intervals for the caching handler
    private static final int INTERVAL_DURATION = 10;
    private static final int INTERVAL_START_DURATION = 10;
    private static final TimeUnit INTERVAL_TIME_UNIT = TimeUnit.SECONDS;

    private final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
    private ThreadPoolExecutor executorPool = new ThreadPoolExecutor(
            EXECUTING_THREADS,
            EXECUTING_THREADS,
            THREAD_TIMEOUT,
            TimeUnit.SECONDS,
            this.workQueue);

    private ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
    private BackendHandler backendHandler;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.backendHandler = new CachingBackendHandler(getString(R.string.source_base_url), getString(R.string.source_type), MAX_CACHE_TIME);

        // Start the repeating check for outdated caches.
        this.scheduledExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                ((CachingBackendHandler)backendHandler).checkForOutdatedCaches();
            }
        }, INTERVAL_START_DURATION, INTERVAL_DURATION, INTERVAL_TIME_UNIT);

        // Recreate the service when there is enough memory, if the OS decides to destroy
        // the service because of low memory.
        return START_STICKY;
    }

    /**
     * Required for unit testing.
     */
    public boolean awaitTermination() throws InterruptedException {
        return this.executorPool.awaitTermination(2, TimeUnit.SECONDS);
    }

    /**
     * Required for unit testing.
     */
    public void shutdown() {
        this.executorPool.shutdown();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BusHandler.getBus().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusHandler.getBus().unregister(this);
        this.executorPool.shutdownNow();
        this.scheduledExecutor.shutdownNow();
    }

    /**
     * Event for near objects. Returns objects near request location as an ReturnNearObjectsEvent.
     *
     * @param event Request event.
     */
    @Subscribe
    public void onEvent(final RequestNearObjectsEvent event) {
        this.executorPool.execute( new Runnable() {
            @Override
            public void run() {
                Log.d(this.getClass().getSimpleName(), "RequestNearObjectsEvent: "+ event.getLocation());

                HandlerResponse response = backendHandler.getObjectsNearLocation(event.getLocation(), event.getRange(), event.isMinimized());
                if (response.isOk()) {
                    BusHandler.getBus().post(new ReturnNearObjectsEvent(response.getObjects()));
                    Log.d(this.getClass().getSimpleName(), "RequestNearObjectsEvent: Sent OK response.");
                } else {
                    BusHandler.getBus().post(new RequestFailedEvent(event, response.getStatus().toString())); // TODO: HandlerResponse reason.
                    Log.d(this.getClass().getSimpleName(), "RequestNearObjectsEvent: Sent FAIL response.");
                }
            }
        });
    }

    /**
     * Event for objects in area. Returns objects in area as an ReturnObjectsInAreaEvent.
     *
     * @param event Request event.
     */
    @Subscribe
    public void onEvent(final RequestObjectsInAreaEvent event) {
        this.executorPool.execute( new Runnable() {
            @Override
            public void run() {
                Log.d(this.getClass().getSimpleName(), "RequestObjectsInAreaEvent: "+ event.getSouthWest() + event.getNorthEast());
                HandlerResponse response = backendHandler.getObjectsInArea(event.getSouthWest(), event.getNorthEast(), event.isMinimized());
                if (response.isOk()) {
                    BusHandler.getBus().post(new ReturnObjectsInAreaEvent(event.getSouthWest(), event.getNorthEast(), response.getObjects()));
                    Log.d(this.getClass().getSimpleName(), "RequestObjectsInAreaEvent: Sent OK response.");
                } else {
                    BusHandler.getBus().post(new RequestFailedEvent(event, response.getStatus().toString())); // TODO: HandlerResponse reason.
                    Log.d(this.getClass().getSimpleName(), "RequestObjectsInAreaEvent: Sent FAIL response.");
                }

            }
        });
    }

    /**
     * Event for objects in area. Doesn't create a return event.
     *
     * @param event Request event.
     */
    @Subscribe
    public void onEvent(final CacheObjectsInAreaEvent event) {
        this.executorPool.execute( new Runnable() {
            @Override
            public void run() {
                if (backendHandler instanceof CachingBackendHandler) {
                    Log.d(this.getClass().getSimpleName(), "CacheObjectsInAreaEvent: "+ event.getSouthWest() + event.getNorthEast());
                    HandlerResponse response = backendHandler.getObjectsInArea(event.getSouthWest(), event.getNorthEast(), event.isMinimized());
                    if (response.isOk()) {
                        Log.d(this.getClass().getSimpleName(), "CacheObjectsInAreaEvent: Sent OK response.");
                    } else {
                        BusHandler.getBus().post(new RequestFailedEvent(event, response.getStatus().toString())); // TODO: HandlerResponse reason.
                        Log.d(this.getClass().getSimpleName(), "CacheObjectsInAreaEvent: Sent FAIL response.");
                    }
                }
            }
        });
    }

    /**
     * Event for requesting detailed information about object. Returns detailed object as
     * ReturnMapObjectEvent.
     *
     * @param event Request event.
     */
    @Subscribe
    public void onEvent(final RequestMapObjectEvent event) {
        this.executorPool.execute( new Runnable() {
            @Override
            public void run() {
                Log.d(this.getClass().getSimpleName(), "RequestMapObjectEvent: "+ event.getId());
                HandlerResponse response = backendHandler.getMapObject(event.getId());
                if (response.isOk()) {
                    MapObject obj = (response.getObjects().size() > 0) ? response.getObjects().get(0) : null;
                    BusHandler.getBus().post(new ReturnMapObjectEvent(obj));
                    Log.d(this.getClass().getSimpleName(), "RequestMapObjectEvent: Sent OK response.");
                } else {
                    BusHandler.getBus().post(new RequestFailedEvent(event, response.getStatus().toString())); // TODO: HandlerResponse reason.
                    Log.d(this.getClass().getSimpleName(), "RequestMapObjectEvent: Sent FAIL response.");
                }
            }
        });
    }


    /**
     * Event for searching objects.
     *
     * @param event Request event.
     */
    @Subscribe
    public void onEvent(final SearchObjectsEvent event) {
        this.executorPool.execute( new Runnable() {
            @Override
            public void run() {
                Log.d(this.getClass().getSimpleName(), "SearchObjectsEvent");
                HandlerResponse response = backendHandler.getObjectsFromSearch(event.getFromFields(),
                        event.getSearchString(), event.getLimit(), event.isMini());
                if (response.isOk()) {
                    BusHandler.getBus().post(new ReturnSearchResultEvent(response.getObjects()));
                    Log.d(this.getClass().getSimpleName(), "SearchObjectsEvent: Sent OK response.");
                } else {
                    BusHandler.getBus().post(new RequestFailedEvent(event, response.getStatus().toString())); // TODO: HandlerResponse reason.
                    Log.d(this.getClass().getSimpleName(), "SearchObjectsEvent: Sent FAIL response.");
                }
            }
        });
    }
}
