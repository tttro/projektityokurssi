package fi.lbd.mobile.mapobjects;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fi.lbd.mobile.SelectionManager;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.CacheObjectsInAreaEvent;
import fi.lbd.mobile.events.RequestObjectsInAreaEvent;
import fi.lbd.mobile.location.ImmutablePointLocation;

/**
 * Class which handles the dependencies between the map objects and markers. Class also updates
 * the model and requests new map objects if they are required.
 *
 * Created by Tommi.
 */
public class MapModelController<T> implements MapTableModelListener<T>, CameraChangeListener {
    public static final double DIVIDE_GRID_LAT = 0.0025;
    public static final double DIVIDE_GRID_LON = 0.005;
    private final int hideMarkersZoom;

    private MarkerProducer<T> markerProducer;
    private MapTableModel<T> tableModel;
    private boolean isMarkersHidden = false;
    private BiMap<T, MapObject> markerObjectMap;
    private MapObject activeMarker;
    private List<ProgressListener> progressListeners = new ArrayList<>();
    private boolean isLoading = false;


    /**
     * Class which handles the dependencies between the map objects and markers. Class also updates
     * the model and requests new map objects if they are required.
     *
     * @param markerProducer    Instance which creates the markers, removes them and performs
     *                          needed actions to them. Used to decouple the map implementation
     *                          from this class.
     * @param hideMarkersZoom   Level at which the markers should be hidden.
     */
    public MapModelController(@NonNull MarkerProducer<T> markerProducer, int hideMarkersZoom) {
        this.markerProducer = markerProducer;
        this.tableModel = new MapTableModel<>(DIVIDE_GRID_LAT, DIVIDE_GRID_LON);
        this.tableModel.addListener( this );
        this.markerObjectMap = HashBiMap.create();
        this.activeMarker = null;
        this.hideMarkersZoom = hideMarkersZoom;
    }

    public void addProgressListener(ProgressListener listener) {
        this.progressListeners.add(listener);
    }

    public void removeProgressListener(ProgressListener listener) {
        this.progressListeners.remove(listener);
    }

    private void notifyStartLoading() {
        for (ProgressListener listener : this.progressListeners) {
            listener.startLoading();
        }
    }

    private void notifyFinishLoading() {
        for (ProgressListener listener : this.progressListeners) {
            listener.finishLoading();
        }
    }

    /**
     * Invoked when a map object is removed from the model.
     *
     * @param obj   Map object which is being removed.
     */
    @Override
    public void objectRemoved(T obj) {
        this.markerObjectMap.remove(obj);
        MapObject mapObject = this.findMapObject(obj);
        if (mapObject != null) {
            this.markerObjectMap.inverse().remove(mapObject);
        }
        this.markerProducer.remove(obj);
    }

    /**
     * Model invokes this method if with the information about the grid which will most likely
     * be loaded next.
     *
     * @param latGridStart  Grid cell start coordinate.
     * @param lonGridStart  Grid cell start coordinate.
     * @param latGridEnd    Grid cell end coordinate.
     * @param lonGridEnd    Grid cell end coordinate.
     */
    @Override
    public void requestCache(double latGridStart, double lonGridStart, double latGridEnd, double lonGridEnd) {
//            Log.d(this.getClass().getSimpleName(), "Cache from area: grid: " + latGridStart + ", " + lonGridStart);
        BusHandler.getBus().post(new CacheObjectsInAreaEvent(
                new ImmutablePointLocation(latGridStart, lonGridStart),
                new ImmutablePointLocation(latGridEnd, lonGridEnd)));
    }

    /**
     * Model invokes this method for cells that needs to be loaded and added to the model. This
     * method only sends an event to the background service which then responds with the objects.
     * Respond is handled in another method and the returned objects are added to the model.
     *
     * @param latGridStart  Grid cell start coordinate.
     * @param lonGridStart  Grid cell start coordinate.
     * @param latGridEnd    Grid cell end coordinate.
     * @param lonGridEnd    Grid cell end coordinate.
     */
    @Override
    public void requestObjects(double latGridStart, double lonGridStart, double latGridEnd, double lonGridEnd) {
//            Log.d(this.getClass().getSimpleName(), "Get from area: grid: " + latGridStart + ", " + lonGridStart);
        BusHandler.getBus().post(new RequestObjectsInAreaEvent(
                new ImmutablePointLocation(latGridStart, lonGridStart),
                new ImmutablePointLocation(latGridEnd, lonGridEnd)));
        if (!this.isLoading) {
            this.isLoading = true;
            this.notifyStartLoading();
        }
    }


    /**
     * Updates the model if the map camera has moved. Also checks if the map is zoomed too far
     * away and hides the map objects if needed.
     *
     * @param zoom  Current zoom level.
     * @param startLat  Start coordinate.
     * @param startLon  Start coordinate.
     * @param endtLat   End coordinate.
     * @param endLon    End coordinate.
     */
    @Override
    public void cameraChanged(double zoom, double startLat, double startLon, double endtLat, double endLon) {
//            Log.d(this.getClass().getSimpleName(), "Camera moved: " + map.getProjection().getVisibleRegion().latLngBounds + " Zoom: "+ cameraPosition.zoom);
        if (zoom <= this.hideMarkersZoom) {
            if (!this.isMarkersHidden) {
                Iterator<T> iter = this.markerObjectMap.keySet().iterator();
                while (iter.hasNext()) {
                    T obj = iter.next();
                    this.markerProducer.event(obj, MarkerProducer.Event.HIDE);
                }
                this.isMarkersHidden = true;
            }
            Log.d(this.getClass().getSimpleName(), "Zoomed too far, hiding markers.");
        } else {
            if (this.isMarkersHidden) {
                Iterator<T> iter = this.markerObjectMap.keySet().iterator();
                while (iter.hasNext()) {
                    T obj = iter.next();
                    this.markerProducer.event(obj, MarkerProducer.Event.SHOW);
                }
                this.isMarkersHidden = false;
                Log.d(this.getClass().getSimpleName(), "Zoomed in, showing markers.");
            }
            this.tableModel.updateTable(startLat, startLon, endtLat, endLon);
        }
    }


    /**
     * Map objects are used to create markers which are then added to the model
     * in the correct grid cell. If the currently selected object is added to the map,
     * change its icon and open its info window.
     *
     * @param objects   Map objects to add
     * @param startLat  Grid cell start.
     * @param startLon  Grid cell start.
     */
    public void processObjects(List<MapObject> objects, double startLat, double startLon) {
        if (objects != null && this.tableModel.isEmpty(startLat, startLon)) {
            List<T> markers = new ArrayList<>();
            for (MapObject mapObject : objects) {

                T obj = this.markerProducer.produce(mapObject.getId(),
                        mapObject.getPointLocation().getLatitude(),
                        mapObject.getPointLocation().getLongitude());

                markers.add(obj);
                this.markerObjectMap.put(obj, mapObject);

                if(SelectionManager.get().getSelectedObject() != null &&
                        mapObject.getId().equals(SelectionManager.get().getSelectedObject().getId())){
                    setActiveMarker(obj);
                    this.markerProducer.event(obj, MarkerProducer.Event.SHOW_INFO);
                } else {
                    this.markerProducer.event(obj, MarkerProducer.Event.INACTIVE);
                }
            }
            this.tableModel.addObjects(startLat, startLon, markers);
        }
        if (this.isLoading && !this.tableModel.hasGridCellsWaitingForObjects()) {
            this.isLoading = false;
            this.notifyFinishLoading();
        }
    }

    /**
     * Clears the currently selected active marker.
     */
    public void clearActiveMarker() {
        if(this.activeMarker != null){
            T marker = this.findMarker(this.activeMarker);
            if(marker != null) {
                this.markerProducer.event(marker, MarkerProducer.Event.INACTIVE);
            }
        }
    }

    /**
     * Sets a current active marker.
     *
     * @param marker    Marker which will be set as active.
     */
    public void setActiveMarker(T marker) {
        clearActiveMarker();
        this.activeMarker = findMapObject(marker);
        this.markerProducer.event(marker, MarkerProducer.Event.ACTIVE);
    }

    public T findMarker(MapObject object){
        return (object == null) ? null : this.markerObjectMap.inverse().get(object);
    }

    public MapObject findMapObject(T marker){
        return (marker == null) ? null : this.markerObjectMap.get(marker);
    }
}

