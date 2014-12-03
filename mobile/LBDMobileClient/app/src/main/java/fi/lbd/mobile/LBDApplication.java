package fi.lbd.mobile;

import android.app.Application;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

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
        SelectionManager.initialize();
        Log.d("MAIN APPLICATION CONTEXT-----------", "ALL INITIALIZED");
    }

}

