package fi.lbd.mobile.backendhandler;

import java.util.List;
import java.util.logging.Handler;

import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Used as a response object from the BackendHandlers.
 *
 * Created by Tommi.
 */
public class HandlerResponse {
    public enum Status {
        Succeeded, Failed, Cached
    }

    private final List<MapObject> objects;
    private final Status status;

    /**
     * Used as a response object from the BackendHandlers.
     *
     * @param objects   Response objects.
     * @param status    Response status.
     */
    public HandlerResponse(List<MapObject> objects, Status status) {
        this.objects = objects;
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public List<MapObject> getObjects() {
        return objects;
    }

    public boolean isOk() {
        return this.status == Status.Succeeded || this.status == Status.Cached;
    }


}
