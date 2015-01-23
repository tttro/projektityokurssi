package fi.lbd.mobile;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains details about the currently selected urls and collections.
 *
 * Created by Tommi on 3.1.2015.
 */
public class ApplicationDetails {
    private final static String LOCATIONDATA_URL = "locationdata/api/";
    private final static String MESSAGEDATA_URL = "messagedata/api/";

    private static ApplicationDetails singleton;
    private List<ApplicationDetailListener> apiListeners;

    private String currentCollection;
    private String currentBaseUrl;
    private String currentObjectApiUrl;
    private String currentMessageApiUrl;

    private String email;

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


    /**
     * Sets the current base api url. Also sets the object api and message api urls.
     *
     * @param currentBaseUrl
     */
    public void setCurrentBaseApiUrl(String currentBaseUrl){
        Log.e(ApplicationDetails.class.getSimpleName(), "Change currentBaseUrl: " + currentBaseUrl);
        this.currentBaseUrl = currentBaseUrl;
        this.currentObjectApiUrl = currentBaseUrl + LOCATIONDATA_URL;
        this.currentMessageApiUrl = currentBaseUrl + MESSAGEDATA_URL;
        this.notifyListenersApiUrlChange(this.currentBaseUrl, this.currentMessageApiUrl, this.currentObjectApiUrl);
    }

    /**
     * Get current users email
     * @return
     */
    public String getUserEmail() {
        return this.email;
    }

    /**
     * Set current user email
     * @param email
     */
    public void setUserEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the current base api url.
     * @return
     */
    public String getCurrentBaseApiUrl(){
        return this.currentBaseUrl;
    }

    /**
     * Returns the current object api url.
     * @return
     */
    public String getCurrentObjectApiUrl(){
        return this.currentObjectApiUrl;
    }

    /**
     * Returns the current message api url.
     * @return
     */
    public String getCurrentMessageApiUrl(){
        return this.currentMessageApiUrl;
    }

    /**
     * Returns the current collection.
     * @return
     */
    public String getCurrentCollection() {
        return currentCollection;
    }

    /**
     * Set the current collection.
     * @param currentCollection
     */
    public void setCurrentCollection(String currentCollection) {
        this.currentCollection = currentCollection;
        this.notifyCollectionChange(this.currentCollection);
    }

    /**
     * Register a listener to listen the url and collection changes.
     * @param listener
     */
    public void registerApiChangeListener(ApplicationDetailListener listener) {
        this.apiListeners.add(listener);
    }

    /**
     * Unregister a listener.
     * @param listener
     */
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

    /**
     * Listener which can be registered to ApplicationDetails to listen url and collection changes.
     */
    public static interface ApplicationDetailListener {
        public void notifyApiUrlChange(String newBaseUrl, String newMessageApiUrl, String newObjectApiUrl);
        public void notifyCollectionChange(String newCollection);
    }
}
