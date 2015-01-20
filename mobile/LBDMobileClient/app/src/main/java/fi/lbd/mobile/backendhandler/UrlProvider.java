package fi.lbd.mobile.backendhandler;

import android.content.Context;

import fi.lbd.mobile.ApplicationDetails;

/**
 * Listens for application detail changes and provides urls for backend handler.
 *
 * Created by Tommi on 10.1.2015.
 */
public class UrlProvider implements ApplicationDetails.ApplicationDetailListener {
    protected String baseUrl;
    protected String baseObjectUrl;
    protected String baseMessageUrl;
    protected String objectCollection;

    public UrlProvider(String baseUrl, String baseObjectUrl, String baseMessageUrl, String objectCollection) {
        this.baseUrl = baseUrl;
        this.baseObjectUrl = baseObjectUrl;
        this.baseMessageUrl = baseMessageUrl;
        this.objectCollection = objectCollection;

    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getBaseObjectUrl() {
        return baseObjectUrl;
    }

    public String getBaseMessageUrl() {
        return baseMessageUrl;
    }

    public String getObjectCollection() {
        return objectCollection;
    }

    @Override
    public void notifyApiUrlChange(String newBaseUrl, String newMessageApiUrl, String newObjectApiUrl) {
        this.baseUrl = newBaseUrl;
        this.baseObjectUrl = newObjectApiUrl;
        this.baseMessageUrl = newMessageApiUrl;
    }

    @Override
    public void notifyCollectionChange(String newCollection) {
        this.objectCollection = newCollection;
    }
}
