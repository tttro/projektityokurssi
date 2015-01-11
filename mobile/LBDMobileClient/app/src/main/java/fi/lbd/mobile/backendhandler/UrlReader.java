package fi.lbd.mobile.backendhandler;

/**
 * Interface for a class which performs the operations to the rest api.
 *
 * Created by Tommi.
 */
public interface UrlReader {
    /**
     * GET command for the rest api.
     *
     * @param url
     * @return
     */
    UrlResponse get(String url);

    /**
     * POST command for the rest api.
     * @param url
     * @param jsonContents
     * @return
     */
    UrlResponse postJson(String url, String jsonContents);

    /**
     * PUT command for the rest api.
     * @param url
     * @param jsonContents
     * @return
     */
    UrlResponse putJson(String url, String jsonContents);

    /**
     * DELETE command for the rest api.
     *
     * @param url
     * @return
     */
    UrlResponse delete(String url);
}
