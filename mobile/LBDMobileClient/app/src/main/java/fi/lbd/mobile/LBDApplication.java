package fi.lbd.mobile;

import android.app.Application;
import android.util.Log;

import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.mapobjects.MapObjectSelectionManager;
import fi.lbd.mobile.messaging.MessageObjectSelectionManager;
import fi.lbd.mobile.messaging.MessageObjectRepository;

/**
 * Custom application class. Initializes singletons and other static things.
 *
 * Created by Tommi.
 */
public class LBDApplication extends Application {
    public LBDApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        BusHandler.initialize();
        MapObjectSelectionManager.initialize();
        MessageObjectRepository.initialize();
        MessageObjectSelectionManager.initialize();
        ServiceManager.initialize(getApplicationContext());
        GlobalToastMaker.initialize(getApplicationContext());
        ApplicationDetails.initialize();

        Log.d("MAIN APPLICATION CONTEXT-----------", "ALL INITIALIZED");
    }
}

