package fi.lbd.mobile.fragments;

/**
 * Created by tommi on 10.11.2014.
 */
public interface MapTableModelListener<T> {
    void objectRemoved(T obj);
    void requestCache(double latGridStart, double lonGridStart, double latGridEnd, double lonGridEnd);
    void requestObjects(double latGridStart, double lonGridStart, double latGridEnd, double lonGridEnd);
}
