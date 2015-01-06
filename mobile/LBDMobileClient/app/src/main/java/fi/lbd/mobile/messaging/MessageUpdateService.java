package fi.lbd.mobile.messaging;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.messaging.events.RequestUserMessagesEvent;

public class MessageUpdateService extends Service {
    private Timer timer = new Timer();
    private static final long UPDATE_INTERVAL = 30*1000;

    public MessageUpdateService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MessageUpdateService", "Received start id " + startId + ": " + intent);
        // Service should run until it is explicitly stopped, so return sticky
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        startService();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        Log.i(getClass().getSimpleName(), "MessageUpdateService stopped.");
    }

    private void startService() {
        timer.scheduleAtFixedRate(
           new TimerTask() {
               public void run() {
                   Log.d(getClass().getSimpleName(), "***** Sending a new RequestUserMessagesEvent from background service");
                   BusHandler.getBus().post(new RequestUserMessagesEvent());
               }
           }, 1000, UPDATE_INTERVAL);
    }
}
