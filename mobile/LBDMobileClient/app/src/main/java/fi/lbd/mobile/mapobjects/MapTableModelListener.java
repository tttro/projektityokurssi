package fi.lbd.mobile.mapobjects;

/**
 * Listener for MapTableModel changes.
 *
 * Created by Tommi.
 */
public interface MapTableModelListener<T> {
    /**
     * Invoked when the parameter object is removed from the model.
     *
     * @param obj   Object which is removed.
     */
    void objectRemoved(T obj);

    /**
     * Invoked when the model wants to cache certain region.
     *
     * @param latGridStart  Start coordinate.
     * @param lonGridStart  Start coordinate.
     * @param latGridEnd    End coordinate.
     * @param lonGridEnd    End coordinate.
     */
    void requestCache(double latGridStart, double lonGridStart, double latGridEnd, double lonGridEnd);

    /**
     * Invoked when the model wants to load objects for the given region. Objects should be set with
     * MapTableModel addObjects method.
     *
     * @param latGridStart  Start coordinate.
     * @param lonGridStart  Start coordinate.
     * @param latGridEnd    End coordinate.
     * @param lonGridEnd    End coordinate.
     */
    void requestObjects(double latGridStart, double lonGridStart, double latGridEnd, double lonGridEnd);
}
