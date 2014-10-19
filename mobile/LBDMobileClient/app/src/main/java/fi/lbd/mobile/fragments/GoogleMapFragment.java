package fi.lbd.mobile.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import fi.lbd.mobile.R;
import fi.lbd.mobile.mapobjects.MapObject;

// TODO: Kesken koko luokka
public class GoogleMapFragment extends MapFragment {
	private MapView mapView;
	private GoogleMap map;
    private List<MapObject> currentObjects = new ArrayList<MapObject>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.googlemap_fragment, container, false);

		// Finds the map view from the map_fragment layout
		mapView = (MapView)view.findViewById(R.id.mapview);
		mapView.onCreate(savedInstanceState);
		
		// Gets to GoogleMap from the MapView and does initialization stuff
		map = mapView.getMap();
		map.getUiSettings().setMyLocationButtonEnabled(false);
		map.setMyLocationEnabled(true);
        MapsInitializer.initialize(this.getActivity());

        boolean first = true;
        for (MapObject obj : this.currentObjects) {
            final LatLng location = new LatLng(obj.getPointLocation().latitude, obj.getPointLocation().longitude);
            Marker marker = map.addMarker(new MarkerOptions().position(location).title("Tolppa"));

            if (first) {
                CameraPosition cameraPosition = CameraPosition.builder()
                        .target(location)
                        .zoom(13)
                        .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),2000, null);
                first = false;
            }
        }
		return view;
	}

    @Override
    public void setCurrentMapObjects(List<MapObject> currentObjects) {
        this.currentObjects.clear();
        this.currentObjects.addAll(currentObjects);
    }

	@Override
	public void onResume() {
		mapView.onResume();
		super.onResume();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
	}
}
