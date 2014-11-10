package fi.lbd.mobile;

import android.app.Application;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import fi.lbd.mobile.events.BusHandler;

/**
 * Created by tommi on 22.10.2014.
 */
public class LBDApplication extends Application {

    // FIXME: POIS KUN EI ENÄÄ TARTTE:
    public static String testData;

    public LBDApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO: Dependency injection?
        //"the static variables instances are bound to the class loader of the class that first initialized them"
        BusHandler.initialize();
        lueTestiData();
        SelectionManager.initialize();
        Log.d("MAIN APPLICATION CONTEXT-----------", "ALL INITIALIZED");
    }

    // FIXME: POIS KUN EI ENÄÄ TARTTE:
    private void lueTestiData() {
        AssetManager assetManager = getAssets();
        InputStream input;
        try {
            input = assetManager.open("dataa");
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            input.close();
            testData = new String(buffer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

