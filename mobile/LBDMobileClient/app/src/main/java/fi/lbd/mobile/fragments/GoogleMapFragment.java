package fi.lbd.mobile.fragments;

import android.content.Intent;
import android.location.Geocoder;
import android.location.Address;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.LocationClient;
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
import fi.lbd.mobile.mapobjects.ImmutablePointLocation;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.PointLocation;


// TODO: Tartteeko markereita niputtaa?: https://github.com/mg6maciej/android-maps-extensions
public class GoogleMapFragment extends MapFragment implements OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {
	private MapView mapView;
	private GoogleMap map;
    private EditText searchText;
    private LocationClient mLocationClient;
    private MapTableModel<Marker> tableModel;
    private BiMap<Marker, MapObject> markerObjectMap; // TODO: Saisko suoraan markeriin liitettyä?
    private Marker activeMarker;
    private Geocoder geocoder;

    public static GoogleMapFragment newInstance(){
        return new GoogleMapFragment();
    }

    @Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.googlemap_fragment, container, false);
		this.mapView = (MapView)view.findViewById(R.id.mapview);
        this.mapView.onCreate(savedInstanceState);
        this.activeMarker = null;
        this.geocoder = new Geocoder(getActivity(), Locale.getDefault());

        this.searchText = (EditText)view.findViewById(R.id.searchText);
        // Hide keyboard and blinking cursor when "enter" or "back" key is pressed on soft keyboard
        this.searchText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    hideKeyBoard();
                    hideCursor();
                    return true;
                }
                else if(keyCode == KeyEvent.KEYCODE_BACK) {
                    hideKeyBoard();
                    hideCursor();
                    return true;
                }
                else {
                    return false;
                }
            }
        });

        this.map = this.mapView.getMap();
        this.map.getUiSettings().setMyLocationButtonEnabled(true);
        this.map.setMyLocationEnabled(true);
        this.map.setOnInfoWindowClickListener(this);
        this.map.setOnMarkerClickListener(this);
        this.map.setOnMapClickListener(this);
        this.map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker marker) {

                // Getting view from the layout file info_window_layout
                View v = inflater.inflate(R.layout.map_info_view, null);

                TextView objectIdField = (TextView) v.findViewById(R.id.objectid);
                objectIdField.setText(marker.getTitle());

                TextView coordinatesField = (TextView) v.findViewById(R.id.coordinates);
                coordinatesField.setText("[" + marker.getPosition().latitude + ", " + marker.getPosition().longitude + "]");

                TextView infoField = (TextView) v.findViewById(R.id.info);
                infoField.setText(Html.fromHtml(marker.getSnippet()));
                return v;
            }
        });

        // Search a location by address
        final Button searchButton = (Button)view.findViewById(R.id.searchButton);
        if (searchButton != null){
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideKeyBoard();
                    hideCursor();

                    if (searchText != null){
                        String address = (searchText).getText().toString();

                        // TODO: Handle incorrect searches
                        try {
                            List<Address> addresses = geocoder.getFromLocationName(address, 1);
                            if (addresses.size() > 0) {
                                Double lat = (double) (addresses.get(0).getLatitude());
                                Double lon = (double) (addresses.get(0).getLongitude());
                                final LatLng location = new LatLng(lat, lon);

                                CameraUpdate cameraLocation = CameraUpdateFactory.newLatLngZoom(location, 18);
                                map.moveCamera(cameraLocation);
                            }
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        this.markerObjectMap = HashBiMap.create();
        this.tableModel = new MapTableModel<>(0.0025, 0.005);
        this.tableModel.addListener( new MapTableModelListener<Marker>() {
            @Override
            public void objectRemoved(Marker obj) {
                markerObjectMap.remove(obj);
                obj.remove();
            }

            @Override
            public void requestCache(double latGridStart, double lonGridStart, double latGridEnd, double lonGridEnd) {
//                Log.e("GoogleMapFragment", "Cache from area: grid: " + latGridStart + ", " + lonGridStart);
                BusHandler.getBus().post(new CacheObjectsInAreaEvent(
                        new ImmutablePointLocation(latGridStart, lonGridStart),
                        new ImmutablePointLocation(latGridEnd, lonGridEnd)));
            }

            @Override
            public void requestObjects(double latGridStart, double lonGridStart, double latGridEnd, double lonGridEnd) {
//                Log.e("GoogleMapFragment", "Get from area: grid: " + latGridStart + ", " + lonGridStart);
                BusHandler.getBus().post(new RequestObjectsInAreaEvent(
                        new ImmutablePointLocation(latGridStart, lonGridStart),
                        new ImmutablePointLocation(latGridEnd, lonGridEnd)));
            }
        });

        this.map.setOnCameraChangeListener( new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
//                Log.e("GoogleMapFragment", "Kamera liikkui: "+ map.getProjection().getVisibleRegion().latLngBounds);
                VisibleRegion region = map.getProjection().getVisibleRegion();
                tableModel.updateTable(region.latLngBounds.southwest.latitude,
                        region.latLngBounds.southwest.longitude,
                        region.latLngBounds.northeast.latitude,
                        region.latLngBounds.northeast.longitude);
            }
        });

        MapsInitializer.initialize(this.getActivity());

        MapObject o = SelectionManager.get().getSelectedObject();
        if(o != null){
            PointLocation location = o.getPointLocation();
            CameraUpdate cameraLocation = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18);
            this.map.moveCamera(cameraLocation);
        }
        // TODO: Käytä käyttäjän sijaintia, täytyy hakea LocationClientilla
        else {
            CameraUpdate cameraLocation = CameraUpdateFactory.newLatLngZoom(new LatLng(61.510990, 23.777361), 16);
            this.map.moveCamera(cameraLocation);
        }
		return view;
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


    @Subscribe
    public void onEvent(ReturnObjectsInAreaEvent event) {
        if (event.getMapObjects() != null
                && this.tableModel.isEmpty(event.getSouthWest().getLatitude(),
                                             event.getSouthWest().getLongitude())) {
            BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromResource(android.R.drawable.presence_invisible);
            BitmapDescriptor markerSelectedIcon = BitmapDescriptorFactory.fromResource(android.R.drawable.presence_online);
            List<Marker> markers = new ArrayList<>();
            for (MapObject mapObject : event.getMapObjects()) {
                BitmapDescriptor icon = markerIcon;
                LatLng location = new LatLng(mapObject.getPointLocation().getLatitude(),
                        mapObject.getPointLocation().getLongitude());

                /*
                if (SelectionManager.get().getSelectedObject() != null &&
                        mapObject.getId().equals(SelectionManager.get().getSelectedObject().getId())) {
                    icon = markerSelectedIcon;
                }*/

//                }
//                StringBuilder snippet = new StringBuilder();
//                for (Map.Entry<String, String> entry : mapObject.getAdditionalProperties().entrySet()) {
//                    snippet.append("<b>");
//                    snippet.append(entry.getKey());
//                    snippet.append(": ");
//                    snippet.append("</b>");
//                    snippet.append(entry.getValue());
//                    snippet.append("<br>");
//                }
//                snippet.append("<br><b><font color=\"blue\">Click for detailed info.</font></b><br>");
                Marker marker = map.addMarker(
                        new MarkerOptions()
                                .position(location)
                                .title(mapObject.getId())
//                                .snippet(snippet.toString())
                                .snippet("<br><b><font color=\"blue\">Click for detailed info.</font></b><br>")
                                .icon(icon));

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

    @Override
    public void onInfoWindowClick(Marker marker) {
        SelectionManager.get().setSelection(findMapObject(marker));
        Intent intent = new Intent(this.getActivity(), DetailsActivity.class);
        startActivity(intent);
    }

    @Subscribe
    public void onEvent(SelectMapObjectEvent event){
//        MapsInitializer.initialize(this.getActivity());
        MapObject o = SelectionManager.get().getSelectedObject();

        if(o != null){
            Marker m = findMarker(o);
            if (m != null) {
                clearActiveMarker();
                setActiveMarker(m);
                PointLocation location = o.getPointLocation();
                CameraUpdate cameraLocation = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18);
                this.map.moveCamera(cameraLocation);
                m.showInfoWindow();
                SelectionManager.get().setSelection(o);
            }
        }
        // TODO: Käytä käyttäjän sijaintia, täytyy hakea LocationClientilla
        else {
            CameraUpdate cameraLocation = CameraUpdateFactory.newLatLngZoom(new LatLng(61.5, 23.795), 16);
            this.map.moveCamera(cameraLocation);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker){
        clearActiveMarker();
        setActiveMarker(marker);

        MapObject mapObject = findMapObject(marker);
        SelectionManager.get().setSelection(mapObject);
        // False for default behavior (center camera and open infowindow)
        return false;
    }

    /* TODO: bugi havaittu kun tarkastellaan kohdetta tietyssä paikassa, siirretään kartta
       jonnekin kauas ja avataan uusi kohde.
       http://stackoverflow.com/questions/21523202/released-unknown-bitmap-reference-setting-marker-in-android
    */
    private void clearActiveMarker() {
        if(activeMarker != null){
            activeMarker.setIcon(BitmapDescriptorFactory.fromResource(android.R.drawable.presence_invisible));
            activeMarker = null;
        }
    }

    public void setActiveMarker(Marker activeMarker) {
        this.activeMarker = activeMarker;
        activeMarker.setIcon(BitmapDescriptorFactory.fromResource(android.R.drawable.presence_online));
    }

    @Override
    public void onMapClick(LatLng point){
        clearActiveMarker();
    }

    public Marker findMarker(MapObject object){
        return (object == null) ? null : this.markerObjectMap.inverse().get(object);
    }

    public MapObject findMapObject(Marker marker){
        return (marker == null) ? null : this.markerObjectMap.get(marker);
    }

    public void hideKeyBoard() {
        if (this.searchText != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    getActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.searchText.getWindowToken(), 0);
        }
    }

    public void hideCursor(){
        if(this.searchText != null) {
            this.searchText.setFocusable(false);
            this.searchText.setFocusableInTouchMode(true);
        }
    }
}
