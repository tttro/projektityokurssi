package fi.lbd.mobile;

import android.app.Application;
import android.util.Log;

import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.mapobjects.MapObjectSelectionManager;
import fi.lbd.mobile.messaging.MessageObjectSelectionManager;
import fi.lbd.mobile.messaging.MessageObjectRepository;

/**
 * Created by Tommi.
 */
public class LBDApplication extends Application {
    public LBDApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO: Dependency injection?
        //"the static variables instances are bound to the class loader of the class that first initialized them"
        BusHandler.initialize();
        MapObjectSelectionManager.initialize();
        MessageObjectRepository.initialize();
        MessageObjectSelectionManager.initialize();
        ServiceManager.initialize(getApplicationContext());
        GlobalToastMaker.initialize(getApplicationContext());
        ApplicationDetails.initialize();
        ApplicationDetails.get().setUserId("108363990223992898018"); // TODO: FIXME
        //ApplicationDetails.get().setCurrentCollection(getString(R.string.streetlights)); // TODO: FIXME

        Log.d("MAIN APPLICATION CONTEXT-----------", "ALL INITIALIZED");
    }
}

