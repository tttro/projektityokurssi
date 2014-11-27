package fi.lbd.mobile.mapobjects;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Model which handles the loading and removing of its objects according to a updated region.
 * Splits the world into grid cells and informs the listeners about regions which should be
 * loaded or cached.
 *
 * Created by Tommi.
 */
public class MapTableModel<T> {

    /**
     * Element which is stored inside the model.
     */
    private class TableElement {
        private int startLat;   // Grid cell start position
        private int startLon;   // Grid cell start position
        private int endLat;   // Grid cell end position
        private int endLon;   // Grid cell end position
        private List<T> objects = new ArrayList<T>();   // Grid cell objects
        private boolean objectsAdded = false;

        public TableElement(int startLat, int startLon, int endLat, int endLon) {
            this.startLat = startLat;
            this.startLon = startLon;
            this.endLat = endLat;
            this.endLon = endLon;
        }

        public void addAll(List<T> objs) {
            this.objects.addAll(objs);
            // Boolean beacause param list can be empty.
            this.objectsAdded = true;
        }


        public boolean hasObjectsAdded() {
            return this.objectsAdded;
        }

        public void clear() {
            for (T o : this.objects) {
                notifyRemovedObject(o);
            }
            this.objects.clear();
        }

        public boolean isEmpty() {
            return this.objects.isEmpty();
        }

        public int getStartLat() {
            return this.startLat;
        }

        public int getStartLon() {
            return this.startLon;
        }

        public int getEndLat() {
            return this.endLat;
        }

        public int getEndLon() {
            return this.endLon;
        }
    }

    // Constants used in caching
    private static final double CACHE_MULTIPLIER_LAT = 0.4;
    private static final double CACHE_MULTIPLIER_LON = 0.4;

    // Actual model which holds the elements in the grid
    private Table<Integer, Integer, TableElement> objectTable = HashBasedTable.create();
    private List<MapTableModelListener<T>> listeners = new ArrayList<>();

    // Used in dividing the grid
    private double latPrecision;
    private double lonPrecision;
    private int latMultiplier;
    private int lonMultiplier;

    // Used while checking if caching is required
    private int lastCachedStartLat;
    private int lastCachedStartLon;
    private int lastCachedEndLat;
    private int lastCachedEndLon;

    private int screenCurrentStartLat;
    private int screenCurrentStartLon;
    private int screenCurrentEndLat;
    private int screenCurrentEndLon;

    /**
     * Model which handles the loading and removing of its objects according to a updated region.
     * Splits the world into grid cells and informs the listeners about regions which should be
     * loaded or cached.
     *
     * @param precisionLat  Used while dividing latitude coordinates.
     * @param precisionLon  Used while dividing longitude coordinates.
     */
    public MapTableModel(double precisionLat, double precisionLon) {
        this.latPrecision = precisionLat;
        this.lonPrecision = precisionLon;
        this.latMultiplier = (int)(1 / this.latPrecision);
        this.lonMultiplier = (int)(1 / this.lonPrecision);
    }

    /**
     * Adds a listener for listening model changes and requests.
     *
     * @param listener  Listener.
     */
    public void addListener(MapTableModelListener<T> listener) {
        this.listeners.add(listener);
    }

    private void notifyRemovedObject(T obj) {
        for (MapTableModelListener<T> l : this.listeners) {
            l.objectRemoved(obj);
        }
    }

    private void notifyRequestCache(double latGridStart, double lonGridStart, double latGridEnd, double lonGridEnd) {
        for (MapTableModelListener<T> l : this.listeners) {
            l.requestCache(latGridStart, lonGridStart, latGridEnd, lonGridEnd);
        }
    }

    private void notifyRequestObjects(double latGridStart, double lonGridStart, double latGridEnd, double lonGridEnd) {
        for (MapTableModelListener<T> l : this.listeners) {
            l.requestObjects(latGridStart, lonGridStart, latGridEnd, lonGridEnd);
        }
    }

    /**
     * Checks if the region has changed enough so that more grid cells should be loaded.
     *
     * @param startLat  Current start position.
     * @param startLon  Current start position.
     * @param endLat  Current end position.
     * @param endLon  Current end position.
     */
    public void updateTable(double startLat, double startLon, double endLat, double endLon) {
//        Log.d(this.getClass().getSimpleName(), "Update map table model: Start: "+ startLat +", "+ startLon +" End:"+ endLat +", "+ endLon );
        int screenStartLat = transformToGridCoordinateLat(startLat);
        int screenStartLon = transformToGridCoordinateLon(startLon);
        int screenEndLat = transformToGridCoordinateLat(endLat);
        int screenEndLon = transformToGridCoordinateLon(endLon);

        checkForCellsOutsideBounds(screenStartLat, screenStartLon, screenEndLat, screenEndLon);

        // If the current screen has changed enough, loop the area covered by
        // the screen and request new objects.
        if (this.screenCurrentStartLat != screenStartLat ||
            this.screenCurrentStartLon != screenStartLon ||
            this.screenCurrentEndLat != screenEndLat ||
            this.screenCurrentEndLon != screenEndLon) {
            for (int lat = screenStartLat; lat <= screenEndLat; lat++) {
                for (int lon = screenStartLon; lon <= screenEndLon; lon++) {
                    double latGridStart = transformToRealCoordinateLat(lat);
                    double lonGridStart = transformToRealCoordinateLon(lon);
                    double latGridEnd = transformToRealCoordinateLat(lat + 1);
                    double lonGridEnd = transformToRealCoordinateLon(lon + 1);
                    if (this.objectTable.get(lat, lon) == null) {
                        this.objectTable.put(lat, lon, new TableElement(screenStartLat, screenStartLon,
                                screenEndLat, screenEndLon));
                        notifyRequestObjects(latGridStart, lonGridStart, latGridEnd, lonGridEnd);
                    }
                }
            }
            this.screenCurrentStartLat = screenStartLat;
            this.screenCurrentStartLon = screenStartLon;
            this.screenCurrentEndLat = screenEndLat;
            this.screenCurrentEndLon = screenEndLon;
        }

        checkForCaching(startLat, startLon, endLat, endLon,
                screenStartLat, screenStartLon, screenEndLat, screenEndLon);
    }

    /**
     * Tries to check if certain grid region should be cached.
     *
     * TODO: Diagonals?
     * TODO: Bug: Tries to cache the previous grid element?
     *
     * @param startLat  Current start position.
     * @param startLon  Current start position.
     * @param endLat  Current end position.
     * @param endLon  Current end position.
     * @param screenStartLat    Rounded start position.
     * @param screenStartLon    Rounded start position.
     * @param screenEndLat    Rounded end position.
     * @param screenEndLon    Rounded end position.
     */
    private void checkForCaching(double startLat, double startLon, double endLat, double endLon,
            int screenStartLat, int screenStartLon, int screenEndLat, int screenEndLon) {

        double latThreshold = this.latPrecision*CACHE_MULTIPLIER_LAT;
        double lonThreshold = this.lonPrecision*CACHE_MULTIPLIER_LON;
        boolean cacheLeft = false;
        boolean cacheRight = false;
        boolean cacheUp = false;
        boolean cacheDown = false;

        if (transformToGridCoordinateLon(startLon - lonThreshold) < this.screenCurrentStartLon
                && this.lastCachedStartLon != this.screenCurrentStartLon-1) {
            cacheLeft = true;
            this.lastCachedStartLon = this.screenCurrentStartLon-1;
        }

        if (transformToGridCoordinateLon(endLon + lonThreshold) > this.screenCurrentEndLon
                && this.lastCachedEndLon != this.screenCurrentEndLon+1) {
            cacheRight = true;
            this.lastCachedEndLon = this.screenCurrentEndLon+1;
        }

        if (transformToGridCoordinateLat(startLat - latThreshold) < this.screenCurrentStartLat
                && this.lastCachedStartLat != this.screenCurrentStartLat-1) {
            cacheDown = true;
            this.lastCachedStartLat = this.screenCurrentStartLat-1;
        }

        if (transformToGridCoordinateLat(endLat + latThreshold) > this.screenCurrentEndLat
                && this.lastCachedEndLat != this.screenCurrentEndLat+1) {
            cacheUp = true;
            this.lastCachedEndLat = this.screenCurrentEndLat+1;
        }

        if (cacheRight) {
            for (int lat = screenStartLat; lat <= screenEndLat; lat++) {
                notifyRequestCache(
                        transformToRealCoordinateLat(lat),
                        transformToRealCoordinateLon(this.screenCurrentEndLon + 1),
                        transformToRealCoordinateLat(lat + 1),
                        transformToRealCoordinateLon(this.screenCurrentEndLon + 2));
            }
        }
        if (cacheLeft) {
            for (int lat = screenStartLat; lat <= screenEndLat; lat++) {
                notifyRequestCache(
                        transformToRealCoordinateLat(lat),
                        transformToRealCoordinateLon(this.screenCurrentStartLon - 1),
                        transformToRealCoordinateLat(lat + 1),
                        transformToRealCoordinateLon(this.screenCurrentStartLon));
            }
        }
        if (cacheUp) {
            for (int lon = screenStartLon; lon <= screenEndLon; lon++) {
                notifyRequestCache(
                        transformToRealCoordinateLat(this.screenCurrentEndLat + 1),
                        transformToRealCoordinateLon(lon),
                        transformToRealCoordinateLat(this.screenCurrentEndLat + 2),
                        transformToRealCoordinateLon(lon + 1));
            }
        }
        if (cacheDown) {
            for (int lon = screenStartLon; lon <= screenEndLon; lon++) {
                notifyRequestCache(
                        transformToRealCoordinateLat(this.screenCurrentStartLat - 1),
                        transformToRealCoordinateLon(lon),
                        transformToRealCoordinateLat(this.screenCurrentStartLat),
                        transformToRealCoordinateLon(lon + 1));
            }
        }
    }

    /**
     * Transforms grid cell coordinate into real coordinate
     * @param gridCoordinate
     * @return
     */
    private double transformToRealCoordinateLon(int gridCoordinate) {
        return ((double)gridCoordinate)/((double)this.lonMultiplier);
    }

    /**
     * Transforms grid cell coordinate into real coordinate
     * @param gridCoordinate
     * @return
     */
    private double transformToRealCoordinateLat(int gridCoordinate) {
        return ((double)gridCoordinate)/((double)this.latMultiplier);
    }

    /**
     * Transforms a real coordinate to a grid cell coordinate.
     * @param val
     * @return
     */
    private int transformToGridCoordinateLat(double val) {
        return (int)(val * this.latMultiplier);
    }

    /**
     * Transforms a real coordinate to a grid cell coordinate.
     * @param val
     * @return
     */
    private int transformToGridCoordinateLon(double val) {
        return (int)(val * this.lonMultiplier);
    }

    /**
     * Checks if any region is outside the current bounds.
     *
     * @param screenStartLat    Current screen bounds.
     * @param screenStartLon    Current screen bounds.
     * @param screenEndLat    Current screen bounds.
     * @param screenEndLon    Current screen bounds.
     */
    private void checkForCellsOutsideBounds(int screenStartLat, int screenStartLon, int screenEndLat, int screenEndLon) {
        Iterator<TableElement> iter = this.objectTable.values().iterator();
        while (iter.hasNext()) {
            TableElement mapElement = iter.next();
            if (isOutsideBounds(mapElement,
                    screenStartLat, screenStartLon,
                    screenEndLat, screenEndLon)) {
                mapElement.clear();
                iter.remove();
            }
        }
    }

    private boolean isOutsideBounds(TableElement tableElement, int screenStartLat, int screenStartLon,
                                         int screenEndLat, int screenEndLon) {
        if (screenEndLat < tableElement.getStartLat() ||
                screenEndLon < tableElement.getStartLon()) {
            return true;
        }
        if (screenStartLat > tableElement.getEndLat() ||
                screenStartLon > tableElement.getEndLon()) {
            return true;
        }
        return false;
    }

    /**
     * Adds objects in the given coordinate to the corresponding grid cell. Coordinate can be any
     * coordinate inside the cell.
     *
     * @param gridStartLat  Any coordinate inside the grid cell.
     * @param gridStartLon  Any coordinate inside the grid cell.
     * @param objects   Objects to add into the cell.
     */
    public void addObjects(double gridStartLat, double gridStartLon, List<T> objects) {
        int gridElementStartLat = this.transformToGridCoordinateLat(gridStartLat);
        int gridElementStartLon = this.transformToGridCoordinateLon(gridStartLon);
        TableElement tableElement = this.objectTable.get(gridElementStartLat, gridElementStartLon);
        if ( tableElement != null ) {
            tableElement.addAll(objects);
        }
    }

    /**
     * Does the model contain grid cells which have requested for objects but haven't received them
     * yet.
     *
     * @return  True if there is a cell which is waiting for objects.
     */
    public boolean hasGridCellsWaitingForObjects() {
        for (TableElement element : this.objectTable.values()) {
            if (!element.hasObjectsAdded()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is grid cell which contains the given position empty.
     *
     * @param gridStartLat  Any coordinate inside the grid cell.
     * @param gridStartLon  Any coordinate inside the grid cell.
     * @return  True if empty, false otherwise.
     */
    public boolean isEmpty(double gridStartLat, double gridStartLon) {
        int gridElementStartLat = this.transformToGridCoordinateLat(gridStartLat);
        int gridElementStartLon = this.transformToGridCoordinateLon(gridStartLon);
        TableElement elem = this.objectTable.get(gridElementStartLat, gridElementStartLon);
        return (elem == null || elem.isEmpty());
    }
}
