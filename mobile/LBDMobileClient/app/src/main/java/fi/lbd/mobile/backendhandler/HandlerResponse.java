package fi.lbd.mobile.backendhandler;

import java.util.List;
import java.util.logging.Handler;

import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Used as a response object from the BackendHandlers.
 *
 * Created by Tommi.
 */
public class HandlerResponse<T> {
    public enum Status {
        Succeeded, Failed, Cached
    }

    private final List<T> objects;
    private final Status status;
    private final String reason;


    /**
     * Used as a response object from the BackendHandlers.
     *
     * @param objects   Response objects.
     * @param status    Response status.
     */
    public HandlerResponse(List<T> objects, Status status) {
        this(objects, status, "");
    }

    /**
     * Used as a response object from the BackendHandlers.
     *
     * @param objects   Response objects.
     * @param status    Response status.
     * @param reason    Response reason.
     */
    public HandlerResponse(List<T> objects, Status status, String reason) {
        this.objects = objects;
        this.status = status;
        this.reason = reason;
    }

    public Status getStatus() {
        return status;
    }

    public List<T> getObjects() {
        return objects;
    }

    public String getReason() { return this.reason; }

    public boolean isOk() {
        return this.status == Status.Succeeded || this.status == Status.Cached;
    }


}
