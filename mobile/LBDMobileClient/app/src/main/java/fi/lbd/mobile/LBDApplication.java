package fi.lbd.mobile;

import android.app.Application;
import android.util.Log;

import fi.lbd.mobile.events.BusHandler;

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
        MessageObjectSelectionManager.initialize();
        Log.d("MAIN APPLICATION CONTEXT-----------", "ALL INITIALIZED");
    }

}

