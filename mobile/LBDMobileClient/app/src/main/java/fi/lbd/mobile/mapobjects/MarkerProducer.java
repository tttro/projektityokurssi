package fi.lbd.mobile.mapobjects;

/**
 * Used to produce markers for a certain model controller. Used to abstract models from some map
 * implementation.
 *
 * Created by Tommi.
 */
public interface MarkerProducer<T> {
    enum Event {SHOW, HIDE, ACTIVE, INACTIVE, SHOW_INFO};

    /**
     * Method which produces a marker with the given details.
     * @param id
     * @param latitude
     * @param longitude
     * @return
     */
    T produce(String id, double latitude, double longitude);

    /**
     * Removes the given marker.
     * @param obj
     */
    void remove(T obj);

    /**
     * Causes an event on the given marker.
     * @param obj
     * @param evt
     */
    void event(T obj, Event evt);
}
