package fi.lbd.mobile.mapobjects;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
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
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.MapTableModel;
import fi.lbd.mobile.mapobjects.MapTableModelListener;
import fi.lbd.mobile.mapobjects.ProgressListener;

/**
 * Class which handles the dependencies between the map objects and markers. Class also updates
 * the model and requests new map objects if they are required.
 *
 * Created by Tommi.
 */
public class MapModelController implements MapTableModelListener<Marker>, GoogleMap.OnCameraChangeListener {
    private static final double DIVIDE_GRID_LAT = 0.0025;
    private static final double DIVIDE_GRID_LON = 0.005;
    private final int hideMarkersZoom;
    private final BitmapDescriptor iconSelected;
    private final BitmapDescriptor iconDefault;

    private GoogleMap map;
    private MapTableModel<Marker> tableModel;
    private boolean isMarkersHidden = false;
    private BiMap<Marker, MapObject> markerObjectMap;
    private MapObject activeMarker;
    private List<ProgressListener> progressListeners = new ArrayList<>();
    private boolean isLoading = false;

    /**
     * Class which handles the dependencies between the map objects and markers. Class also updates
     * the model and requests new map objects if they are required.
     *
     * @param map   GoogleMap which is monitored
     */
    public MapModelController(@NonNull GoogleMap map, BitmapDescriptor iconSelected, BitmapDescriptor iconDefault, int hideMarkersZoom) {
        this.map = map;
        this.tableModel = new MapTableModel<>(DIVIDE_GRID_LAT, DIVIDE_GRID_LON);
        this.tableModel.addListener( this );
        this.map.setOnCameraChangeListener( this );
        this.markerObjectMap = HashBiMap.create();
        this.activeMarker = null;
        this.hideMarkersZoom = hideMarkersZoom;
        this.iconDefault = iconDefault;
        this.iconSelected = iconSelected;
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
    public void objectRemoved(Marker obj) {
        this.markerObjectMap.remove(obj);
        obj.remove();
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
     * @param cameraPosition    Current camera position.
     */
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
//            Log.d(this.getClass().getSimpleName(), "Camera moved: " + map.getProjection().getVisibleRegion().latLngBounds + " Zoom: "+ cameraPosition.zoom);
        if (cameraPosition.zoom <= this.hideMarkersZoom) {
            if (!this.isMarkersHidden) {
                Iterator<Marker> iter = this.markerObjectMap.keySet().iterator();
                while (iter.hasNext()) {
                    Marker marker = iter.next();
                    marker.setVisible(false);
                }
                // TODO: Notification to the user that the markers are hidden?
                this.isMarkersHidden = true;
            }
        } else {
            if (this.isMarkersHidden) {
                Iterator<Marker> iter = this.markerObjectMap.keySet().iterator();
                while (iter.hasNext()) {
                    Marker marker = iter.next();
                    marker.setVisible(true);
                }
                this.isMarkersHidden = false;
            }
            VisibleRegion region = this.map.getProjection().getVisibleRegion();
            this.tableModel.updateTable(region.latLngBounds.southwest.latitude,
                    region.latLngBounds.southwest.longitude,
                    region.latLngBounds.northeast.latitude,
                    region.latLngBounds.northeast.longitude);
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
            List<Marker> markers = new ArrayList<>();
            for (MapObject mapObject : objects) {
                LatLng location = new LatLng(mapObject.getPointLocation().getLatitude(),
                        mapObject.getPointLocation().getLongitude());

                Marker marker = this.map.addMarker(
                        new MarkerOptions()
                                .position(location)
                                .title(mapObject.getId())
                                .icon(this.iconDefault));

                markers.add(marker);
                this.markerObjectMap.put(marker, mapObject);

                if(SelectionManager.get().getSelectedObject() != null &&
                        mapObject.getId().equals(SelectionManager.get().getSelectedObject().getId())){
                    setActiveMarker(marker);
                    marker.showInfoWindow();
                }
            }
            this.tableModel.addObjects(startLat, startLon, markers);
        }
        if (this.isLoading && !this.tableModel.hasGridCellsWaitingForObjects()) {
            this.isLoading = false;
            this.notifyFinishLoading();
        }
    }

    public boolean hasCellsWaitingForObjects() {
        return this.tableModel.hasGridCellsWaitingForObjects();
    }

    /**
     * Clears the currently selected active marker.
     */
    public void clearActiveMarker() {
        if(this.activeMarker != null){
            Marker marker = this.findMarker(this.activeMarker);
            if(marker != null) {
                marker.setIcon(this.iconDefault);
            }
        }
    }

    /**
     * Sets a current active marker.
     *
     * @param marker    Marker which will be set as active.
     */
    public void setActiveMarker(Marker marker) {
        clearActiveMarker();
        this.activeMarker = findMapObject(marker);
        marker.setIcon(this.iconSelected);
    }

    public Marker findMarker(MapObject object){
        return (object == null) ? null : this.markerObjectMap.inverse().get(object);
    }

    public MapObject findMapObject(Marker marker){
        return (marker == null) ? null : this.markerObjectMap.get(marker);
    }
}

