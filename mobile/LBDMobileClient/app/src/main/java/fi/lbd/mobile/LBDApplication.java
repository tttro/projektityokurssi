package fi.lbd.mobile;

import android.app.Application;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.mapobjects.SelectionManager;

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
        SelectionManager.initialize();
        lueTestiData();

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

