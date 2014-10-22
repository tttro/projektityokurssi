package fi.lbd.mobile.repository;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.AbstractEvent;
import fi.lbd.mobile.events.RequestNearObjectsEvent;
import fi.lbd.mobile.events.RequestObjectsInAreaEvent;
import fi.lbd.mobile.events.ReturnNearObjectsEvent;
import fi.lbd.mobile.events.ReturnObjectsInAreaEvent;
import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Service for handling the map object repository. Communication is done through OTTO-bus.
 *
 * Created by tommi on 19.10.2014.
 */
public class MapObjectRepositoryService extends Service {
    private boolean running;
    private Thread serviceThread; // TODO: Thread pooliksi? https://developer.android.com/training/multiple-threads/create-threadpool.html
    private BlockingQueue<AbstractEvent> workQueue;
    private MapObjectRepository repo;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.running = true; // TODO: Tartteeko vai suoraan interrupt?

        this.workQueue = new LinkedBlockingQueue<AbstractEvent>();
        this.repo = new MapObjectRepository();

        Runnable serviceLoop = new Runnable() {
            public void run() {
                while (running) {
                    try {
                        AbstractEvent work = workQueue.take();
                        processEventWithService(work);
                    } catch (InterruptedException e) {
                        // TODO: LOGGING
                    }
                }
            }
        };

        this.serviceThread = new Thread(serviceLoop);
        this.serviceThread.start();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BusHandler.getBus().register(this);
//        Toast.makeText(this, "Service created", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusHandler.getBus().unregister(this);
//        Toast.makeText(this, "Service destroyed", Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onEvent(RequestNearObjectsEvent event) {
        this.workQueue.add(event);
    }

    @Subscribe
    public void onEvent(RequestObjectsInAreaEvent event) {
        this.workQueue.add(event);
    }

    private void processEventWithService(AbstractEvent event) {
        if (Thread.currentThread() != this.serviceThread) {
            throw new RuntimeException("Method processEventWithService was accessed from wrong thread!"); // TODO: String
        }

        if (event instanceof RequestNearObjectsEvent) {
            List<MapObject> objects = this.repo.getObjectsNearLocation(((RequestNearObjectsEvent)event).getLocation());
            BusHandler.getBus().post(new ReturnNearObjectsEvent(objects));

        // TODO:
        } else if (event instanceof RequestObjectsInAreaEvent) {
            List<MapObject> objects = this.repo.getObjectsNearLocation(null);
            BusHandler.getBus().post(new ReturnObjectsInAreaEvent(objects));
        }

    }
}
