package fi.lbd.mobile;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tommi on 3.1.2015.
 */
public class ApplicationDetails {
    private final static String LOCATIONDATA_URL = "locationdata/api/";
    private final static String MESSAGEDATA_URL = "messagedata/api/";

    private static ApplicationDetails singleton;
    private List<ApplicationDetailListener> apiListeners;

    private String userId;
    private String currentCollection;
    private String currentBaseUrl;
    private String currentObjectApiUrl;
    private String currentMessageApiUrl;


    public static void initialize() {
        if (ApplicationDetails.singleton == null) {
            ApplicationDetails.singleton = new ApplicationDetails();
        }
    }

    public static ApplicationDetails get() {
        return ApplicationDetails.singleton;
    }

    private ApplicationDetails() {
        this.apiListeners = new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCurrentBaseApiUrl(String currentBaseUrl){
        Log.e(ApplicationDetails.class.getSimpleName(), "Change currentBaseUrl: " + currentBaseUrl);
        this.currentBaseUrl = currentBaseUrl;
        this.currentObjectApiUrl = currentBaseUrl + LOCATIONDATA_URL;
        this.currentMessageApiUrl = currentBaseUrl + MESSAGEDATA_URL;
        this.notifyListenersApiUrlChange(this.currentBaseUrl, this.currentMessageApiUrl, this.currentObjectApiUrl);
    }


    public String getCurrentBaseApiUrl(){
        return this.currentBaseUrl;
    }

    public String getCurrentObjectApiUrl(){
        return this.currentObjectApiUrl;
    }

    public String getCurrentMessageApiUrl(){
        return this.currentMessageApiUrl;
    }

    public String getCurrentCollection() {
        return currentCollection;
    }
    public void setCurrentCollection(String currentCollection) {
        this.currentCollection = currentCollection;
        this.notifyCollectionChange(this.currentCollection);
    }

    public void registerApiChangeListener(ApplicationDetailListener listener) {
        this.apiListeners.add(listener);
    }

    public void unregisterApiChangeListener(ApplicationDetailListener listener) {
        this.apiListeners.remove(listener);
    }

    private void notifyListenersApiUrlChange(String newBaseUrl, String newMessageApiUrl, String newObjectApiUrl) {
        for (ApplicationDetailListener listener : this.apiListeners) {
            listener.notifyApiUrlChange( newBaseUrl, newMessageApiUrl, newObjectApiUrl);
        }
    }

    private void notifyCollectionChange(String newCollection) {
        for (ApplicationDetailListener listener : this.apiListeners) {
            listener.notifyCollectionChange(newCollection);
        }
    }

    public static interface ApplicationDetailListener {
        public void notifyApiUrlChange(String newBaseUrl, String newMessageApiUrl, String newObjectApiUrl);
        public void notifyCollectionChange(String newCollection);
    }
}
