package fi.lbd.mobile.messaging;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import fi.lbd.mobile.GlobalToastMaker;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.messaging.events.DeleteMessageFromListEvent;
import fi.lbd.mobile.messaging.events.RequestUserMessagesEvent;
import fi.lbd.mobile.messaging.events.ReturnUserMessagesEvent;
import fi.lbd.mobile.messaging.events.UpdateCachedMessagesToListEvent;
import fi.lbd.mobile.messaging.messageobjects.MessageObject;

/**
 *
 * Service that requests a new list of user's messages every 30 seconds.
 *
 * If new message list differs from the previous message list, new messages are saved to
 * MessageObjectRepository, and MessageFragment is notified via UpdateCachedMessagesToListEvent.
 *
 * Created by Ossi.
 */
public class MessageUpdateService extends Service {
    private Timer timer = new Timer();
    private static final long UPDATE_INTERVAL = 30*1000;
    private boolean waitingForReturnEvent;

    public MessageUpdateService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MessageUpdateService", "Received start id " + startId + ": " + intent);
        // Service should run until it is explicitly stopped, so return sticky
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        BusHandler.getBus().register(this);
        startService();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        BusHandler.getBus().unregister(this);
        if (timer != null) {
            timer.cancel();
        }
        Log.i(getClass().getSimpleName(), " MessageUpdateService stopped.");
    }

    private void startService() {
        timer.scheduleAtFixedRate(
           new TimerTask() {
               public void run() {
                   Log.d(getClass().toString(), " Sending a new RequestUserMessagesEvent from background service");
                   if (!waitingForReturnEvent) {
                       BusHandler.getBus().post(new RequestUserMessagesEvent());
                       waitingForReturnEvent = true;
                   }
               }
           }, 500, UPDATE_INTERVAL);
    }

    @Subscribe
    public void onEvent(ReturnUserMessagesEvent event) {
        waitingForReturnEvent = false;
        List<MessageObject> newMessageObjects = event.getMessageObjects();
        List<MessageObject> oldMessageObjects = MessageObjectRepository.get().getObjects();

        // If null received, we do nothing
        if(newMessageObjects == null){}
        else if (!MessageObjectRepository.areMessageListsIdentical(oldMessageObjects, newMessageObjects)) {
            newMessageObjects = MessageObjectRepository.sortMessagesByTimestamp(newMessageObjects);
            MessageObjectRepository.get().setObjects(newMessageObjects);

            /**
             * Send new objects both via @Produce annotation as well as a normal Post event.
             *
             * This way MessageFragment receives updated messages instantaneously if it is active,
             * or otherwise at the moment when it becomes active.
             */
            BusHandler.getBus().post(new UpdateCachedMessagesToListEvent(newMessageObjects));
            this.updateMessagesToList();
            if(newMessageObjects.size() > 0) {
                String dialogText = "You have a new message!";
                GlobalToastMaker.makeLongToast(dialogText);
            }
        }

        // If no message updates are needed, send an update event with null parameter, so that
        // MessageFragment dismisses its "Loading messages" dialog.
        else {
            BusHandler.getBus().post(new UpdateCachedMessagesToListEvent(null));
        }
    }

    @Subscribe
    public void onEvent(DeleteMessageFromListEvent event){
        if(event.getMessageId() != null) {
            Log.d("MessageUpdateService deleting message with ID ", event.getMessageId());
            MessageObjectRepository.get().deleteMessage(event.getMessageId());
            BusHandler.getBus().post(new UpdateCachedMessagesToListEvent(MessageObjectRepository.get().getObjects()));
            updateMessagesToList();
        }
    }

    @Produce
    public UpdateCachedMessagesToListEvent updateMessagesToList(){
        Log.d("MessageUpdateService ", "producing UpdateCachedMessagesEvent");
        return new UpdateCachedMessagesToListEvent(MessageObjectRepository.get().getObjects());
    }
}
