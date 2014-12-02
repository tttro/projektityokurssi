package fi.lbd.mobile.mapobjects;

/**
 * Created by Tommi.
 */
public interface MarkerProducer<T> {
    enum Event {SHOW, HIDE, ACTIVE, INACTIVE, SHOW_INFO};

    T produce(String id, double latitude, double longitude);
    void remove(T obj);
    void event(T obj, Event evt);
}
