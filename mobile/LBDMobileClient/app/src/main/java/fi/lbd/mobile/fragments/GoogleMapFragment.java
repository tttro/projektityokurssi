package fi.lbd.mobile.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import fi.lbd.mobile.DetailsActivity;
import fi.lbd.mobile.R;
import fi.lbd.mobile.SelectionManager;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.CacheObjectsInAreaEvent;
import fi.lbd.mobile.events.RequestObjectsInAreaEvent;
import fi.lbd.mobile.events.ReturnObjectsInAreaEvent;
import fi.lbd.mobile.events.SelectMapObjectEvent;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.location.LocationHandler;
import fi.lbd.mobile.location.LocationUtils;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.location.PointLocation;
import fi.lbd.mobile.mapobjects.MapTableModel;
import fi.lbd.mobile.mapobjects.MapTableModelListener;


/**
 * Fragment which handles the google map view.
 *
 * Created by Tommi & Ossi
 */
public class GoogleMapFragment extends MapFragment implements OnInfoWindowClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    // Icons for the map markers
    private static BitmapDescriptor iconSelected;
    private static BitmapDescriptor iconDefault;

	private MapView mapView;
	private GoogleMap map;
    private EditText searchLocationField;
    private Geocoder geocoder;
    private MapModelController modelController;
    private LocationHandler locationHandler;


    /**
     * Should be used to instantiate this class instead of new-statement.
     * @return  Returns a new instance of this class.
     */
    public static GoogleMapFragment newInstance(){
        return new GoogleMapFragment(); // Constructor should not have additional parameters!
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.googlemap_fragment, container, false);

		this.mapView = (MapView)view.findViewById(R.id.mapview);
        this.mapView.onCreate(savedInstanceState);
        this.geocoder = new Geocoder(getActivity(), Locale.getDefault());

        this.searchLocationField = (EditText)view.findViewById(R.id.searchText);

        // Hide keyboard and blinking cursor when "enter" or "back" key is pressed on soft keyboard
        this.searchLocationField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    hideKeyBoard();
                    hideCursor();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                    hideKeyBoard();
                    hideCursor();
                    return true;
                }
                return false;
            }
        });

        // Listen to keyboard search button press
        this.searchLocationField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    hideKeyBoard();
                    hideCursor();
                    return true;
                }
                return false;
            }
        });

        this.locationHandler = new LocationHandler(this.getActivity());

        this.map = this.mapView.getMap();
        this.map.getUiSettings().setMyLocationButtonEnabled(true);
        this.map.setMyLocationEnabled(true);
        this.map.setOnInfoWindowClickListener(this);
        this.map.setOnMarkerClickListener(this);
        this.map.setOnMapClickListener(this);
        this.map.setInfoWindowAdapter(new CustomInfoWindow(inflater, this.locationHandler));

        MapsInitializer.initialize(this.getActivity());
        GoogleMapFragment.iconSelected = BitmapDescriptorFactory.fromResource(android.R.drawable.presence_online);
        GoogleMapFragment.iconDefault = BitmapDescriptorFactory.fromResource(android.R.drawable.presence_invisible);

        Resources res = getResources();
        int maxZoom = res.getInteger(R.integer.min_marker_zoom);
        final int defaultZoom =  res.getInteger(R.integer.default_zoom);
        this.modelController = new MapModelController(this.map, maxZoom);

        setMapLocationToSelectedObject();
		return view;
	}

    @Override
    public void onInfoWindowClick(Marker marker) {
        SelectionManager.get().setSelection(this.modelController.findMapObject(marker));
        Intent intent = new Intent(this.getActivity(), DetailsActivity.class);
        startActivity(intent);
        this.getActivity().overridePendingTransition(0, 0);
    }

    @Subscribe
    public void onEvent(SelectMapObjectEvent event){
        setMapLocationToSelectedObject();
    }

    /**
     * Sets the maps location to the map object which is currently selected in selection manager.
     * If selection manager doesn't have any object selected, sets map view to users current location.
     */
    private void setMapLocationToSelectedObject() {
        Resources res = getResources();
        final int defaultZoom =  res.getInteger(R.integer.default_zoom);

        MapObject mapObject = SelectionManager.get().getSelectedObject();
        if(mapObject != null){
            PointLocation location = mapObject.getPointLocation();
            CameraUpdate cameraLocation = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), defaultZoom);
            this.map.moveCamera(cameraLocation);

            Marker marker = this.modelController.findMarker(mapObject);
            if (marker != null) {
                this.modelController.setActiveMarker(marker);
                marker.showInfoWindow();
            }
        }
        // TODO: Käytä käyttäjän sijaintia, täytyy hakea LocationClientilla
        else {
            CameraUpdate cameraLocation = CameraUpdateFactory.newLatLngZoom(new LatLng(61.5, 23.795), defaultZoom);
            this.map.moveCamera(cameraLocation);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker){
        this.modelController.setActiveMarker(marker);

        MapObject mapObject = this.modelController.findMapObject(marker);
        SelectionManager.get().setSelection(mapObject);
        // False for default behavior (center camera and open infowindow)
        return false;
    }

    @Override
    public void onMapClick(LatLng point){
        this.modelController.clearActiveMarker();
        SelectionManager.get().setSelection(null);

        // TODO: eikö sijainnista osoite tarttekkaan api avainta?? Kohtuu hidas, pitäis tehdä async taskissa.
//        try {
//            List <Address> addresses = this.geocoder.getFromLocation(point.latitude, point.longitude, 1);
//            if (addresses.size() > 0) {
//                Log.e(this.getClass().getSimpleName(), "Click location: "+ addresses.get(0) );
//            } else {
//                Log.e(this.getClass().getSimpleName(), "NO LOCATION" );
//            }
//        } catch (IOException e1) {
//            Log.e(this.getClass().getSimpleName(), "IO Exception in getFromLocation()");
//        }

    }

    public void hideKeyBoard() {
        if (this.searchLocationField != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    getActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.searchLocationField.getWindowToken(), 0);
        }
    }

    public void hideCursor(){
        if(this.searchLocationField != null) {
            this.searchLocationField.setFocusable(false);
            this.searchLocationField.setFocusableInTouchMode(true);
        }
    }

    @Override
    public void onResume() {
        this.mapView.onResume();
        super.onResume();
        BusHandler.getBus().register(this);
        BusHandler.getBus().register(this.modelController);
        this.locationHandler.start();
    }

    @Override
    public void onPause() {
        this.mapView.onPause();
        super.onPause();
        BusHandler.getBus().unregister(this);
        BusHandler.getBus().unregister(this.modelController);
        this.locationHandler.stop();
        hideCursor();
        hideKeyBoard();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /**
     * Class which handles the dependencies between the map objects and markers. Class also updates
     * the model and requests new map objects if they are required.
     */
    private static class MapModelController implements MapTableModelListener<Marker>, GoogleMap.OnCameraChangeListener {
        private static final double DIVIDE_GRID_LAT = 0.0025;
        private static final double DIVIDE_GRID_LON = 0.005;
        private final int hideMarkersZoom;

        private GoogleMap map;
        private MapTableModel<Marker> tableModel;
        private boolean isMarkersHidden = false;
        private BiMap<Marker, MapObject> markerObjectMap;
        private MapObject activeMarker;


        /**
         * Class which handles the dependencies between the map objects and markers. Class also updates
         * the model and requests new map objects if they are required.
         *
         * @param map   GoogleMap which is monitored
         */
        public MapModelController(@NonNull GoogleMap map, int hideMarkersZoom) {
            this.map = map;
            this.tableModel = new MapTableModel<>(DIVIDE_GRID_LAT, DIVIDE_GRID_LON);
            this.tableModel.addListener( this );
            this.map.setOnCameraChangeListener( this );
            this.markerObjectMap = HashBiMap.create();
            this.activeMarker = null;
            this.hideMarkersZoom = hideMarkersZoom;
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
//           Log.e("GoogleMapFragment", "Cache from area: grid: " + latGridStart + ", " + lonGridStart);
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
//          Log.e("GoogleMapFragment", "Get from area: grid: " + latGridStart + ", " + lonGridStart);
            BusHandler.getBus().post(new RequestObjectsInAreaEvent(
                    new ImmutablePointLocation(latGridStart, lonGridStart),
                    new ImmutablePointLocation(latGridEnd, lonGridEnd)));
        }

        /**
         * Updates the model if the map camera has moved. Also checks if the map is zoomed too far
         * away and hides the map objects if needed.
         *
         * @param cameraPosition    Current camera position.
         */
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            Log.d(this.getClass().getSimpleName(), "Camera moved: " + map.getProjection().getVisibleRegion().latLngBounds + " Zoom: "+ cameraPosition.zoom);
            if (cameraPosition.zoom <= this.hideMarkersZoom) {
                if (!this.isMarkersHidden) {
                    Iterator<Marker> iter = this.markerObjectMap.keySet().iterator();
                    while (iter.hasNext()) {
                        Marker marker = iter.next();
                        marker.setVisible(false);
                    }
                    //Toast.makeText(getActivity(), "Zoom in to see markers.", Toast.LENGTH_SHORT);
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
         * Handles the response event with the map objects for a certain region. Map objects are
         * used to create markers which are then added to the model in the correct grid cell.
         *
         * @param event Returned event.
         */
        @Subscribe
        public void onEvent(ReturnObjectsInAreaEvent event) {
            if (event.getMapObjects() != null
                    && this.tableModel.isEmpty(event.getSouthWest().getLatitude(), event.getSouthWest().getLongitude())) {

                List<Marker> markers = new ArrayList<>();
                for (MapObject mapObject : event.getMapObjects()) {
                    LatLng location = new LatLng(mapObject.getPointLocation().getLatitude(),
                            mapObject.getPointLocation().getLongitude());

                    Marker marker = this.map.addMarker(
                            new MarkerOptions()
                                    .position(location)
                                    .title(mapObject.getId())
                                    .icon(GoogleMapFragment.iconDefault));

                    markers.add(marker);
                    this.markerObjectMap.put(marker, mapObject);

                    if(SelectionManager.get().getSelectedObject() != null &&
                            mapObject.getId().equals(SelectionManager.get().getSelectedObject().getId())){
                        setActiveMarker(marker);
                        marker.showInfoWindow();
                    }
                }

                this.tableModel.addObjects(event.getSouthWest().getLatitude(),
                        event.getSouthWest().getLongitude(), markers);
            }
        }

        /**
         * Clears the currently selected active marker.
         */
        private void clearActiveMarker() {
            if(this.activeMarker != null){
                Marker marker = this.findMarker(this.activeMarker);
                if(marker != null) {
                    marker.setIcon(GoogleMapFragment.iconDefault);
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
            marker.setIcon(GoogleMapFragment.iconSelected);
        }

        public Marker findMarker(MapObject object){
            return (object == null) ? null : this.markerObjectMap.inverse().get(object);
        }

        public MapObject findMapObject(Marker marker){
            return (marker == null) ? null : this.markerObjectMap.get(marker);
        }
    }

    /**
     * Custom info window for the markers which are displayed on the map.
     */
    private static class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {
        private LayoutInflater inflater;
        private LocationHandler locationHandler;

        /**
         * Custom info window for the markers which are displayed on the map.
         *
         * @param inflater
         */
        public CustomInfoWindow(LayoutInflater inflater, LocationHandler locationHandler) {
            this.inflater = inflater;
            this.locationHandler = locationHandler;
        }

        // http://stackoverflow.com/questions/16144341/android-googlemap-2-update-information-dynamically-in-infowindow-with-imageview
        // Use default InfoWindow frame
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        // Defines the contents of the InfoWindow
        @Override
        public View getInfoContents(Marker marker) {

            // Getting view from the layout file info_window_layout
            View v = this.inflater.inflate(R.layout.map_info_view, null);

            TextView objectIdField = (TextView) v.findViewById(R.id.objectid);
            objectIdField.setText(marker.getTitle());

            TextView distanceField = (TextView) v.findViewById(R.id.distance);
            PointLocation location = this.locationHandler.getLastLocation();
            if (location != null) {
                int distance = ((int) LocationUtils.distanceBetween(
                        marker.getPosition().latitude, marker.getPosition().longitude,
                        location.getLatitude(), location.getLongitude()));
                distanceField.setText("Distance: "+ distance +"m");
            } else {
                distanceField.setText("");
            }
            return v;
        }
    };

    public void performSearch(){
        hideKeyBoard();
        hideCursor();

        if (searchLocationField != null){
            Resources res = getResources();
            final int defaultZoom =  res.getInteger(R.integer.default_zoom);
            String address = (searchLocationField).getText().toString();

            // TODO: Should we limit results within Finland?
            try {
                List<Address> addresses = geocoder.getFromLocationName(address, 1);
                if (addresses.size() > 0) {
                    Double lat = addresses.get(0).getLatitude();
                    Double lon = addresses.get(0).getLongitude();
                    final LatLng location = new LatLng(lat, lon);

                    CameraUpdate cameraLocation = CameraUpdateFactory.newLatLngZoom(location, defaultZoom);
                    map.moveCamera(cameraLocation);
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

}


