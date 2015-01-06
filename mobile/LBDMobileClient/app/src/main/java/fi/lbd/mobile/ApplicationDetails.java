package fi.lbd.mobile;

import android.util.Log;

/**
 * Created by Tommi on 3.1.2015.
 */
public class ApplicationDetails {
    private static ApplicationDetails singleton;

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
    }

    public String getCurrentCollection() {
        return currentCollection;
    }
    public void setCurrentCollection(String currentCollection) {
        this.currentCollection = currentCollection;
    }
}
