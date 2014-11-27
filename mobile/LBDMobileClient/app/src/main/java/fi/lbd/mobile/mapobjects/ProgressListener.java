package fi.lbd.mobile.mapobjects;

/**
 * Used in the MapModelController. Notifies the listeners if the controller is currently waiting for
 * map objects from the bus.
 *
 * Created by Tommi.
 */
public interface ProgressListener {
    /**
     * Invoked when the loading of the map objects starts.
     */
    void startLoading();


    /**
     * Invoked when the loading of the map objects ends.
     */
    void finishLoading();
}
