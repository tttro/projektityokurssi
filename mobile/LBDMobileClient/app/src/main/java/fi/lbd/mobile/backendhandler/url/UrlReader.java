package fi.lbd.mobile.backendhandler.url;

import android.util.Pair;

/**
 * Interface for a class which performs the operations to the rest api.
 *
 * Created by Tommi.
 */
public interface UrlReader {

    /**
     * GET command for the rest api.
     * @param url
     * @param customHeaders
     * @return
     */
    UrlResponse get(String url, Pair<String, String>... customHeaders);

    /**
     * POST command for the rest api.
     * @param url
     * @param jsonContents
     * @param customHeaders
     * @return
     */
    UrlResponse postJson(String url, String jsonContents, Pair<String, String>... customHeaders);

    /**
     * PUT command for the rest api.
     * @param url
     * @param jsonContents
     * @param customHeaders
     * @return
     */
    UrlResponse putJson(String url, String jsonContents, Pair<String, String>... customHeaders);

    /**
     * DELETE command for the rest api.
     *
     * @param url
     * @param customHeaders
     * @return
     */
    UrlResponse delete(String url, Pair<String, String>... customHeaders);
}
