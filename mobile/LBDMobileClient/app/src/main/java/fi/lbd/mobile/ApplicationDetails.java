package fi.lbd.mobile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tommi on 3.1.2015.
 */
public class ApplicationDetails {
    private static ApplicationDetails singleton;
    private List<ApplicationDetailListener> listeners;

    private String userId;
    private String currentCollection;
    private String currentBackendUrl;

    public static void initialize() {
        if (ApplicationDetails.singleton == null) {
            ApplicationDetails.singleton = new ApplicationDetails();
        }
    }

    public static ApplicationDetails get() {
        return ApplicationDetails.singleton;
    }

    private ApplicationDetails() {
        this.listeners = new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCurrentBackendUrl(){
        return this.currentBackendUrl;
    }
    public void setCurrentBackendUrl(String currentBackendUrl){
        this.currentBackendUrl = currentBackendUrl;
        this.notifyListeners(ApplicationDetailListener.EventType.URL_CHANGED, this.currentBackendUrl);
    }

    public String getCurrentCollection() {
        return currentCollection;
    }
    public void setCurrentCollection(String currentCollection) {
        this.currentCollection = currentCollection;
        this.notifyListeners(ApplicationDetailListener.EventType.COLLECTION_CHANGED, this.currentCollection);
    }

    public void registerChangeListener(ApplicationDetailListener listener) {
        this.listeners.add(listener);
    }

    public void unregisterChangeListener(ApplicationDetailListener listener) {
        this.listeners.remove(listener);
    }

    private void notifyListeners(ApplicationDetailListener.EventType eventType, String newValue) {
        for (ApplicationDetailListener listener : this.listeners) {
            listener.notifyApplicationChange(eventType, newValue);
        }
    }

    public static interface ApplicationDetailListener {
        public enum EventType {COLLECTION_CHANGED, URL_CHANGED}
        public void notifyApplicationChange(EventType eventType, String newValue);
    }
}
