package fi.lbd.mobile.fragments;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tommi on 10.11.2014.
 */
public class MapTableModel<T> {

    private class TableElement {
        private int startLat;
        private int startLon;
        private int endLat;
        private int endLon;
        private List<T> objects = new ArrayList<T>();

        public TableElement(int startLat, int startLon, int endLat, int endLon) {
            this.startLat = startLat;
            this.startLon = startLon;
            this.endLat = endLat;
            this.endLon = endLon;
        }

        public void addAll(List<T> objs) {
            this.objects.addAll(objs);
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

//    private class OutsideRegionPredicate implements Predicate<TableElement> {
//        int screenStartLat;
//        int screenStartLon;
//        int screenEndLat;
//        int screenEndLon;
//
//        public OutsideRegionPredicate() {}
//
//        public void setCurrentRegion(int screenStartLat, int screenStartLon,
//                                     int screenEndLat, int screenEndLon) {
//            this.screenStartLat = screenStartLat;
//            this.screenStartLon = screenStartLon;
//            this.screenEndLat = screenEndLat;
//            this.screenEndLon = screenEndLon;
//        }
//
//        @Override
//        public boolean apply(TableElement tableElement) {
//            if (this.screenEndLat < tableElement.getStartLat() ||
//                    this.screenEndLon < tableElement.getStartLon()) {
//                return true;
//            }
//            if (this.screenStartLat > tableElement.getEndLat() ||
//                    this.screenStartLon > tableElement.getEndLon()) {
//                return true;
//            }
//            return false;
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            return o.equals(this);
//        }
//    }

//    private final OutsideRegionPredicate outsideScreenPredicate;
    private Table<Integer, Integer, TableElement> objectTable = HashBasedTable.create();
    private List<MapTableModelListener<T>> listeners = new ArrayList<>();
    private int latMultiplier;
    private int lonMultiplier;

    private double latPrecision;
    private double lonPrecision;

    private int lastCachedStartLat;
    private int lastCachedStartLon;
    private int lastCachedEndLat;
    private int lastCachedEndLon;

    private int screenCurrentStartLat;
    private int screenCurrentStartLon;
    private int screenCurrentEndLat;
    private int screenCurrentEndLon;

    public MapTableModel(double precisionLat, double precisionLon) {
        this.latPrecision = precisionLat;
        this.lonPrecision = precisionLon;
        this.latMultiplier = (int)(1 / this.latPrecision);
        this.lonMultiplier = (int)(1 / this.lonPrecision);
//        this.outsideScreenPredicate = new OutsideRegionPredicate();
    }

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

    public void updateTable(double startLat, double startLon, double endLat, double endLon) {
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


    // TODO: Diagonals?
    // TODO: Bug: Tries to cache the previous grid element?
    private void checkForCaching(double startLat, double startLon, double endLat, double endLon,
            int screenStartLat, int screenStartLon, int screenEndLat, int screenEndLon) {

        double latThreshold = this.latPrecision*0.4;
        double lonThreshold = this.lonPrecision*0.4;
        boolean cacheLeft = false;
        boolean cacheRight = false;
        boolean cacheUp = false;
        boolean cacheDown = false;

        // http://www.latlong.net/
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

    private double transformToRealCoordinateLon(int gridCoordinate) {
        return ((double)gridCoordinate)/((double)this.lonMultiplier);
    }
    private double transformToRealCoordinateLat(int gridCoordinate) {
        return ((double)gridCoordinate)/((double)this.latMultiplier);
    }

    private int transformToGridCoordinateLat(double val) {
        return (int)(val * this.latMultiplier);
    }


    private int transformToGridCoordinateLon(double val) {
        return (int)(val * this.lonMultiplier);
    }

//    public void updateTableOld(double startLat, double startLon, double endLat, double endLon) {
//        // Transform actual coordinates into integers which presents the coordinates in the grid
//        int screenStartLat = (int)(startLat * this.multiplier) - 1;
//        int screenStartLon = (int)(startLon * this.multiplier) - 1;
//        int screenEndLat = (int)(endLat * this.multiplier) + 1;
//        int screenEndLon = (int)(endLon * this.multiplier) + 1;
//
//        checkForCellsOutsideBounds(screenStartLat, screenStartLon, screenEndLat, screenEndLon);
//
//        // Loop through the grid cells in the area covered by the screen
//        for (int lat = screenStartLat; lat <= screenEndLat; lat++) {
//            for (int lon = screenStartLon; lon <= screenEndLon; lon++) {
//                double latGridStart = ((double)lat)/((double)this.multiplier);
//                double lonGridStart = ((double)lon)/((double)this.multiplier);
//                double latGridEnd = ((double)(lat+1))/((double)this.multiplier);
//                double lonGridEnd = ((double)(lon+1))/((double)this.multiplier);
//
//                if (lat == screenStartLat || lat == screenEndLat || lon == screenStartLon || lon == screenEndLon) {
////                    Log.e("GoogleMapFragment", "Cache area: " + lat + ", " + lon + " grid: " + latGridStart + ", " + lonGridStart);
//                    notifyRequestCache(latGridStart, lonGridStart, latGridEnd, lonGridEnd);
//                } else {
//                    if (this.objectTable.get(lat, lon) == null) {
//                        this.objectTable.put(lat, lon, new TableElement(screenStartLat, screenStartLon, screenEndLat, screenEndLon));
//
////                        Log.e("GoogleMapFragment", "Get from area: " + lat + ", " + lon + " grid: " + latGridStart + ", " + lonGridStart);
//                        notifyRequestObjects(latGridStart, lonGridStart, latGridEnd, lonGridEnd);
//                    }
//                }
//
//            }
//        }
//    }

    private void checkForCellsOutsideBounds(int screenStartLat, int screenStartLon, int screenEndLat, int screenEndLon) {
//        this.outsideScreenPredicate.setCurrentRegion(screenStartLat, screenStartLon, screenEndLat, screenEndLon);
//        Iterators.removeIf(this.objectTable.values().iterator(), this.outsideScreenPredicate);

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

    public void addObjects(double gridStartLat, double gridStartLon, List<T> objects) {
        int gridElementStartLat = this.transformToGridCoordinateLat(gridStartLat);
        int gridElementStartLon = this.transformToGridCoordinateLon(gridStartLon);
        TableElement tableElement = this.objectTable.get(gridElementStartLat, gridElementStartLon);
        if ( tableElement != null ) {
            tableElement.addAll(objects);
        }
    }

//    public boolean contains(double gridStartLat, double gridStartLon) {
//        int gridElementStartLat = this.transformToGridCoordinateLat(gridStartLat);
//        int gridElementStartLon = this.transformToGridCoordinateLon(gridStartLon);
//        return this.objectTable.contains(gridElementStartLat, gridElementStartLon);
//    }

    public boolean isEmpty(double gridStartLat, double gridStartLon) {
        int gridElementStartLat = this.transformToGridCoordinateLat(gridStartLat);
        int gridElementStartLon = this.transformToGridCoordinateLon(gridStartLon);
        TableElement elem = this.objectTable.get(gridElementStartLat, gridElementStartLon);
        return (elem == null || elem.isEmpty());
    }
}
