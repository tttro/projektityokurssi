package fi.lbd.mobile.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.Locale;

import fi.lbd.mobile.DetailsActivity;
import fi.lbd.mobile.R;
import fi.lbd.mobile.SelectionManager;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.ReturnObjectsInAreaEvent;
import fi.lbd.mobile.events.SelectMapObjectEvent;
import fi.lbd.mobile.location.ImmutablePointLocation;
import fi.lbd.mobile.location.LocationHandler;
import fi.lbd.mobile.location.LocationUtils;
import fi.lbd.mobile.location.PointLocation;
import fi.lbd.mobile.mapobjects.MapModelController;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.ProgressListener;


/**
 * Fragment which handles the google map view.
 *
 * Created by Tommi & Ossi
 */
public class GoogleMapFragment extends MapFragment implements OnInfoWindowClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private LatLng defaultLocation;

	private MapView mapView;
    private ProgressBar progressBar;
	private GoogleMap map;
    private EditText searchLocationField;
    private Geocoder geocoder;
    private MapModelController modelController;
    private LocationHandler locationHandler;


    /**
     * Should be used to instantiate this class instead of new-statement.
     * @return  Returns a new instance of this class.
     */
    public static GoogleMapFragment newInstance(LocationHandler locationHandler){
        GoogleMapFragment fragment = new GoogleMapFragment();
        fragment.setLocationHandler(locationHandler);
        return fragment; // Constructor should not have additional parameters!
    }

    public void setLocationHandler(LocationHandler locationHandler) {
        this.locationHandler = locationHandler;
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.googlemap_fragment, container, false);

        this.progressBar = (ProgressBar)view.findViewById(R.id.mapProgressBar);
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
                    performLocationSearch();
                    hideKeyBoard();
                    hideCursor();
                    return true;
                }
                return false;
            }
        });

        this.map = this.mapView.getMap();
        this.map.getUiSettings().setMyLocationButtonEnabled(true);
        this.map.setMyLocationEnabled(true);
        this.map.setOnInfoWindowClickListener(this);
        this.map.setOnMarkerClickListener(this);
        this.map.setOnMapClickListener(this);
        this.map.setInfoWindowAdapter(new CustomInfoWindow(getResources(), inflater, this.locationHandler));

        MapsInitializer.initialize(this.getActivity());

        // Set location if users current location is not found.
        this.defaultLocation = new LatLng(61.5, 23.795); // TODO: From resources as int?

        Resources res = getResources();
        int maxZoom = res.getInteger(R.integer.min_marker_zoom);
        this.modelController = new MapModelController(this.map,
                BitmapDescriptorFactory.fromResource(android.R.drawable.presence_online),
                BitmapDescriptorFactory.fromResource(android.R.drawable.presence_invisible),
                maxZoom);
        this.modelController.addProgressListener( new ProgressListener() {
            @Override
            public void startLoading() {
                progressBar.setVisibility(View.VISIBLE);
            }
            @Override
            public void finishLoading() {
                progressBar.setVisibility(View.GONE);
            }
        });

        /**
         * Set the camera location when the location handler succeeds to connect to the
         * play services. Only used when the fragment is first displayed. Sets the maps default
         * start location to the users current location.
         */
        this.locationHandler.addListener( new GooglePlayServicesClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                setMapLocationToSelectedObject();
                locationHandler.removeListener(this);
            }

            @Override
            public void onDisconnected() {}
        });

		return view;
	}

    @Override
    public void onInfoWindowClick(Marker marker) {
        SelectionManager.get().setSelection(this.modelController.findMapObject(marker));
        Intent intent = new Intent(this.getActivity(), DetailsActivity.class);
        startActivity(intent);
        this.getActivity().overridePendingTransition(0, 0); // Hides the transition animation
    }

    @Subscribe
    public void onEvent(SelectMapObjectEvent event){
        setMapLocationToSelectedObject();
    }

    /**
     * Handles the response event with the map objects for a certain region.
     *
     * @param event Returned event.
     */
    @Subscribe
    public void onEvent(ReturnObjectsInAreaEvent event) {
        this.modelController.processObjects(event.getMapObjects(),
                event.getSouthWest().getLatitude(), event.getSouthWest().getLongitude());
    }

    /**
     * Sets the maps location to the map object which is currently selected in selection manager.
     * If selection manager doesn't have any object selected, sets map view to users current location.
     */
    protected void setMapLocationToSelectedObject() {
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
        } else {
            ImmutablePointLocation userLocation = this.locationHandler.getLastLocation();
            if (userLocation != null) {
                CameraUpdate cameraLocation = CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), defaultZoom);
                this.map.moveCamera(cameraLocation);
            } else {
                CameraUpdate cameraLocation = CameraUpdateFactory.newLatLngZoom(this.defaultLocation, defaultZoom);
                this.map.moveCamera(cameraLocation);
            }
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
    }

    protected void hideKeyBoard() {
        if (this.searchLocationField != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    getActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.searchLocationField.getWindowToken(), 0);
        }
    }

    protected void hideCursor(){
        if(this.searchLocationField != null) {
            this.searchLocationField.setFocusable(false);
            this.searchLocationField.setFocusableInTouchMode(true);
        }
    }

    /**
     * Searches the address defined in the search box and moves the camera to the found location.
     */
    protected void performLocationSearch(){
        hideKeyBoard();
        hideCursor();

        if (this.searchLocationField != null){
            Resources res = getResources();
            final int defaultZoom =  res.getInteger(R.integer.default_zoom);
            String address = (this.searchLocationField).getText().toString();

            // TODO: Should we limit results within Finland?
            try {
                List<Address> addresses = this.geocoder.getFromLocationName(address, 1);
                if (addresses.size() > 0) {
                    Double lat = addresses.get(0).getLatitude();
                    Double lon = addresses.get(0).getLongitude();
                    final LatLng location = new LatLng(lat, lon);

                    CameraUpdate cameraLocation = CameraUpdateFactory.newLatLngZoom(location, defaultZoom);
                    this.map.moveCamera(cameraLocation);
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        this.mapView.onResume();
        super.onResume();
        BusHandler.getBus().register(this);
    }

    @Override
    public void onPause() {
        this.mapView.onPause();
        super.onPause();
        BusHandler.getBus().unregister(this);
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
     * Custom info window for the markers which are displayed on the map.
     */
    private static class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {
        private LayoutInflater inflater;
        private LocationHandler locationHandler;
        private Resources res;

        /**
         * Custom info window for the markers which are displayed on the map.
         *
         * @param inflater
         */
        public CustomInfoWindow(Resources res, LayoutInflater inflater, LocationHandler locationHandler) {
            this.inflater = inflater;
            this.locationHandler = locationHandler;
            this.res = res;
        }

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
                distanceField.setText(res.getString(R.string.distance, LocationUtils.formatDistance(distance)));
            } else {
                distanceField.setText("");
            }
            return v;
        }
    };

}


