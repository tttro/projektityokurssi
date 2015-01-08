package fi.lbd.mobile.backendhandler;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;

import com.squareup.otto.Subscribe;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import fi.lbd.mobile.ApplicationDetails;
import fi.lbd.mobile.R;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestFailedEvent;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.events.CacheObjectsInAreaEvent;
import fi.lbd.mobile.mapobjects.events.RequestMapObjectEvent;
import fi.lbd.mobile.mapobjects.events.RequestNearObjectsEvent;
import fi.lbd.mobile.mapobjects.events.RequestObjectsInAreaEvent;
import fi.lbd.mobile.mapobjects.events.ReturnMapObjectEvent;
import fi.lbd.mobile.mapobjects.events.ReturnNearObjectsEvent;
import fi.lbd.mobile.mapobjects.events.ReturnObjectsInAreaEvent;
import fi.lbd.mobile.mapobjects.events.ReturnSearchResultEvent;
import fi.lbd.mobile.mapobjects.events.SearchObjectsEvent;
import fi.lbd.mobile.messaging.messageobjects.MessageObject;
import fi.lbd.mobile.messaging.events.DeleteMessageEvent;
import fi.lbd.mobile.messaging.events.DeleteMessageSucceededEvent;
import fi.lbd.mobile.events.RequestCollectionsEvent;
import fi.lbd.mobile.messaging.events.RequestUserMessagesEvent;
import fi.lbd.mobile.events.RequestUsersEvent;
import fi.lbd.mobile.events.ReturnCollectionsEvent;
import fi.lbd.mobile.messaging.events.ReturnUserMessagesEvent;
import fi.lbd.mobile.events.ReturnUsersEvent;
import fi.lbd.mobile.messaging.events.SendMessageEvent;
import fi.lbd.mobile.messaging.events.SendMessageSucceededEvent;

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

    private final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
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

        // Read backend certificate from file
        Certificate certificate = null;
        InputStream certificateInput = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            certificateInput = new BufferedInputStream(this.getApplicationContext().getAssets().open("lbd.crt"));
            certificate = cf.generateCertificate(certificateInput);
        } catch (CertificateException | IOException ex) {
            Log.e(BasicUrlReader.class.getSimpleName(), "ERROR", ex);
        } finally {
            if (certificateInput != null) {
                try {
                    certificateInput.close();
                } catch (IOException ex) {
                    Log.e(BasicUrlReader.class.getSimpleName(), "Failed to close certificate input stream!");
                }
            }
        }

        if (certificate == null) {
            throw new RuntimeException("Failed to read certificate from file!");
        }

        BasicUrlReader urlReader = new BasicUrlReader();
        try {
            urlReader.initialize(new Pair<>("LBDServiceCertificate", certificate));
        } catch (UrlReaderException ex) {
            throw new RuntimeException(ex.getOriginalException());
        }

        this.backendHandler = new CachingBackendHandler(urlReader, getString(R.string.source_objects_base_url),
                getString(R.string.source_messages_base_url), MAX_CACHE_TIME);
        if (this.backendHandler instanceof ApplicationDetails.ApplicationDetailListener) {
            ApplicationDetails.get().registerChangeListener((ApplicationDetails.ApplicationDetailListener)this.backendHandler);
        }

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

        if (this.backendHandler instanceof ApplicationDetails.ApplicationDetailListener) {
            ApplicationDetails.get().unregisterChangeListener((ApplicationDetails.ApplicationDetailListener)this.backendHandler);
        }
    }

    /**
     * Event for near objects. Returns objects near request location as an ReturnNearObjectsEvent.
     *
     * @param event Request event.
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(final RequestNearObjectsEvent event) {
        this.executorPool.execute( new Runnable() {
            @Override
            public void run() {
                Log.d(this.getClass().getSimpleName(), "RequestNearObjectsEvent: "+ event.getLocation());

                HandlerResponse<MapObject> response = backendHandler.getObjectsNearLocation(ApplicationDetails.get().getCurrentCollection(),
                        event.getLocation(), event.getRange(), event.isMinimized());
                if (response.isOk()) {
                    BusHandler.getBus().post(new ReturnNearObjectsEvent(response.getObjects()));
                    Log.d(this.getClass().getSimpleName(), "RequestNearObjectsEvent: Sent OK response.");
                } else {
                    BusHandler.getBus().post(new RequestFailedEvent(event, response.getReason()));
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
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(final RequestObjectsInAreaEvent event) {
        this.executorPool.execute( new Runnable() {
            @Override
            public void run() {
                Log.d(this.getClass().getSimpleName(), "RequestObjectsInAreaEvent: "+ event.getSouthWest() + event.getNorthEast());
                HandlerResponse<MapObject> response = backendHandler.getObjectsInArea(ApplicationDetails.get().getCurrentCollection(),
                        event.getSouthWest(), event.getNorthEast(), event.isMinimized());
                if (response.isOk()) {
                    BusHandler.getBus().post(new ReturnObjectsInAreaEvent(event.getSouthWest(), event.getNorthEast(), response.getObjects()));
                    Log.d(this.getClass().getSimpleName(), "RequestObjectsInAreaEvent: Sent OK response.");
                } else {
                    BusHandler.getBus().post(new RequestFailedEvent(event, response.getReason()));
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
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(final CacheObjectsInAreaEvent event) {
        this.executorPool.execute( new Runnable() {
            @Override
            public void run() {
                if (backendHandler instanceof CachingBackendHandler) {
                    Log.d(this.getClass().getSimpleName(), "CacheObjectsInAreaEvent: "+ event.getSouthWest() + event.getNorthEast());
                    HandlerResponse<MapObject> response = backendHandler.getObjectsInArea(ApplicationDetails.get().getCurrentCollection(),
                            event.getSouthWest(), event.getNorthEast(), event.isMinimized());
                    if (response.isOk()) {
                        Log.d(this.getClass().getSimpleName(), "CacheObjectsInAreaEvent: Sent OK response.");
                    } else {
                        BusHandler.getBus().post(new RequestFailedEvent(event, response.getReason()));
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
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(final RequestMapObjectEvent event) {
        this.executorPool.execute( new Runnable() {
            @Override
            public void run() {
                Log.d(this.getClass().getSimpleName(), "RequestMapObjectEvent: "+ event.getId());
                HandlerResponse<MapObject> response = backendHandler.getMapObject(ApplicationDetails.get().getCurrentCollection(), event.getId());
                if (response.isOk()) {
                    MapObject obj = (response.getObjects().size() > 0) ? response.getObjects().get(0) : null;
                    BusHandler.getBus().post(new ReturnMapObjectEvent(obj));
                    Log.d(this.getClass().getSimpleName(), "RequestMapObjectEvent: Sent OK response.");
                } else {
                    BusHandler.getBus().post(new RequestFailedEvent(event, response.getReason()));
                    Log.d(this.getClass().getSimpleName(), "RequestMapObjectEvent: Sent FAIL response.");
                }
            }
        });
    }

    /**
     * Event for searching objects.
     *
     * @param event Search event.
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(final SearchObjectsEvent event) {
        this.executorPool.execute( new Runnable() {
            @Override
            public void run() {
                Log.d(this.getClass().getSimpleName(), "SearchObjectsEvent");
                HandlerResponse<MapObject> response = backendHandler.getObjectsFromSearch(ApplicationDetails.get().getCurrentCollection(),
                        event.getFromFields(), event.getSearchString(), event.getLimit(), event.isMini());

                if (response.isOk()) {
                    BusHandler.getBus().post(new ReturnSearchResultEvent(response.getObjects()));
                    Log.d(this.getClass().getSimpleName(), "SearchObjectsEvent: Sent OK response.");
                } else {
                    BusHandler.getBus().post(new RequestFailedEvent(event, response.getReason()));
                    Log.d(this.getClass().getSimpleName(), "SearchObjectsEvent: Sent FAIL response.");
                }
            }
        });
    }

    /**
     * Event for fetching list of authorized users.
     *
     * @param event Request event.
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(final RequestUsersEvent event){
        this.executorPool.execute(new Runnable(){
            @Override
            public void run(){
                Log.d(this.getClass().getSimpleName(), "RequestUsersEvent");
                HandlerResponse<String> response = backendHandler.getUsers();

                if (response.isOk()) {
                    BusHandler.getBus().post(new ReturnUsersEvent(response.getObjects()));
                    Log.d(this.getClass().getSimpleName(), "RequestUsersEvent: Sent OK response.");
                } else {
                    BusHandler.getBus().post(new RequestFailedEvent(event, response.getReason()));
                    Log.d(this.getClass().getSimpleName(), "RequestUsersEvent: Sent FAIL response.");
                }
            }
        });
    }


    /**
     * Event for fetching list of object collections in the database.
     *
     * @param event Request event.
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(final RequestCollectionsEvent event){
        this.executorPool.execute(new Runnable(){
            @Override
            public void run(){
                Log.d(this.getClass().getSimpleName(), "RequestCollectionsEvent");
                HandlerResponse<String> response = backendHandler.getCollections(event.getUrl());

                if (response.isOk()) {
                    BusHandler.getBus().post(new ReturnCollectionsEvent(response.getObjects()));
                    Log.d(this.getClass().getSimpleName(), "RequestCollectionsEvent: Sent OK response.");
                } else {
                    BusHandler.getBus().post(new RequestFailedEvent(event, response.getReason()));
                    Log.d(this.getClass().getSimpleName(), "RequestCollectionsEvent: Sent FAIL response.");
                }
            }
        });
    }


    /**
     * Event for fetching user messages.
     *
     * @param event Request event.
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(final RequestUserMessagesEvent event) {
        this.executorPool.execute( new Runnable() {
            @Override
            public void run() {
                Log.d(this.getClass().getSimpleName(), "RequestUserMessagesEvent");
                HandlerResponse<MessageObject> response = backendHandler.getMessages();

                if (response.isOk()) {
                    BusHandler.getBus().post(new ReturnUserMessagesEvent(response.getObjects()));
                    Log.d(this.getClass().getSimpleName(), "RequestUserMessagesEvent: Sent OK response.");
                } else {
                    BusHandler.getBus().post(new RequestFailedEvent(event, response.getReason()));
                    Log.d(this.getClass().getSimpleName(), "RequestUserMessagesEvent: Sent FAIL response.");
                }
            }
        });
    }


    /**
     * Event for sending messages which are string type.
     *
     * @param event Send event.
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(final SendMessageEvent<String> event) {
        this.executorPool.execute( new Runnable() {
            @Override
            public void run() {
                Log.d(this.getClass().getSimpleName(), "SendMessageEvent");

                HandlerResponse<MessageObject> response = backendHandler.postMessage(ApplicationDetails.get().getCurrentCollection(),
                        event.getReceiver(), event.getTopic(), event.getMessage(), event.getObjectAttachments());

                if (response.isOk()) {
                    BusHandler.getBus().post(new SendMessageSucceededEvent(event));
                    Log.d(this.getClass().getSimpleName(), "SendMessageEvent: Sent OK response.");
                } else {
                    BusHandler.getBus().post(new RequestFailedEvent(event, response.getReason()));
                    Log.d(this.getClass().getSimpleName(), "SendMessageEvent: Sent FAIL response.");
                }
            }
        });
    }


    /**
     * Event for deleting messages
     *
     * @param event Send event.
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(final DeleteMessageEvent event) {
        this.executorPool.execute( new Runnable() {
            @Override
            public void run() {
                Log.d(this.getClass().getSimpleName(), "DeleteMessageEvent");

                HandlerResponse<MessageObject> response = backendHandler.deleteMessage(ApplicationDetails.get().getCurrentCollection(),
                        event.getMessageId());

                if (response.isOk()) {
                    BusHandler.getBus().post(new DeleteMessageSucceededEvent(event));
                    Log.d(this.getClass().getSimpleName(), "DeleteMessageEvent: Sent OK response.");
                } else {
                    BusHandler.getBus().post(new RequestFailedEvent(event, response.getReason()));
                    Log.d(this.getClass().getSimpleName(), "DeleteMessageEvent: Sent FAIL response.");
                }
            }
        });
    }
}
