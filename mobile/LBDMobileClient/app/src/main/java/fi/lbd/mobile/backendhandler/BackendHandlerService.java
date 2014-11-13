package fi.lbd.mobile.backendhandler;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import fi.lbd.mobile.events.AbstractEvent;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.CacheObjectsInAreaEvent;
import fi.lbd.mobile.events.RequestMapObjectEvent;
import fi.lbd.mobile.events.RequestNearObjectsEvent;
import fi.lbd.mobile.events.RequestObjectsInAreaEvent;
import fi.lbd.mobile.events.ReturnMapObjectEvent;
import fi.lbd.mobile.events.ReturnNearObjectsEvent;
import fi.lbd.mobile.events.ReturnObjectsInAreaEvent;
import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Service for handling the map object repository. Communication is done through OTTO-bus.
 *
 * Created by tommi on 19.10.2014.
 */
public class BackendHandlerService extends Service {
//    Runtime.getRuntime().availableProcessors();

    private static final String SOURCE_BASE_URL = "http://lbdbackend.ignorelist.com/locationdata/api/";
    private static final String SOURCE_TYPE = "Streetlights/";

    private static final int EXECUTING_THREADS = 4;
    private static final int THREAD_TIMEOUT = 10;

    private ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);

    private final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
    private final ThreadPoolExecutor executorPool = new ThreadPoolExecutor(
            EXECUTING_THREADS,
            EXECUTING_THREADS,
            THREAD_TIMEOUT,
            TimeUnit.SECONDS,
            this.workQueue);

    private CachingBackendHandler backendHandler;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.backendHandler = new CachingBackendHandler(SOURCE_BASE_URL, SOURCE_TYPE);

        this.scheduledExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                backendHandler.checkForOutdatedCaches();
            }
        }, 10, 10, TimeUnit.SECONDS);
//        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
        return START_STICKY;
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

    @Subscribe
    public void onEvent(RequestNearObjectsEvent event) {
        addEventToQueue(event);
    }

    @Subscribe
    public void onEvent(RequestObjectsInAreaEvent event) {
        addEventToQueue(event);
    }

    @Subscribe
    public void onEvent(CacheObjectsInAreaEvent event) {
        addEventToQueue(event);
    }

    @Subscribe
    public void onEvent(RequestMapObjectEvent event) {
        addEventToQueue(event);
    }

    private void addEventToQueue(final AbstractEvent event) {
        this.executorPool.execute( new Runnable() {
            @Override
            public void run() {
                processEventWithExecutor(event);
            }
        });
    }

    private void processEventWithExecutor(AbstractEvent event) {
        if (event instanceof RequestNearObjectsEvent) {
            RequestNearObjectsEvent casted = (RequestNearObjectsEvent)event;
            List<MapObject> objects = this.backendHandler.getObjectsNearLocation(casted.getLocation(), casted.getRange(), casted.isMinimized());
            BusHandler.getBus().post(new ReturnNearObjectsEvent(objects));

        } else if (event instanceof RequestObjectsInAreaEvent) {
            RequestObjectsInAreaEvent casted = (RequestObjectsInAreaEvent)event;
            List<MapObject> objects = this.backendHandler.getObjectsInArea(casted.getSouthWest(), casted.getNorthEast(), casted.isMinimized());
            BusHandler.getBus().post(new ReturnObjectsInAreaEvent(casted.getSouthWest(), casted.getNorthEast(), objects));

        // TODO: Tsekkaus onko caching handler?
        } else if (event instanceof CacheObjectsInAreaEvent) {
            CacheObjectsInAreaEvent casted = (CacheObjectsInAreaEvent)event;
            List<MapObject> objects = this.backendHandler.getObjectsInArea(casted.getSouthWest(), casted.getNorthEast(), casted.isMinimized());

        } else if (event instanceof RequestMapObjectEvent) {
            RequestMapObjectEvent casted = (RequestMapObjectEvent)event;
            MapObject object = this.backendHandler.getMapObject(casted.getId());
            BusHandler.getBus().post(new ReturnMapObjectEvent(object));
        }

    }
}
