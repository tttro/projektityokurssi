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

import fi.lbd.mobile.R;

public class GoogleMapFragment extends Fragment {
	private MapView mapView;
	private GoogleMap map;

	public GoogleMapFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//this.setRetainInstance(true);
		View view = inflater.inflate(R.layout.googlemap_fragment, container, false);
		
		// Finds the map view from the map_fragment layout
		mapView = (MapView)view.findViewById(R.id.mapview);
		mapView.onCreate(savedInstanceState);
		
		// Gets to GoogleMap from the MapView and does initialization stuff
		map = mapView.getMap();
		map.getUiSettings().setMyLocationButtonEnabled(false);
		map.setMyLocationEnabled(true);

		// Custom marker: 
		// http://www.nasc.fr/android/android-using-layout-as-custom-marker-on-google-map-api/
		// http://stackoverflow.com/questions/14811579/android-map-api-v2-custom-marker-with-imageview
		final LatLng CIU = new LatLng(getArguments().getDouble("latitude"), getArguments().getDouble("longitude"));    
		Marker testMarker = map.addMarker(new MarkerOptions().position(CIU).title("Tolppa"));  
		// testMarker.remove(); // Removes marker from map

		MapsInitializer.initialize(this.getActivity());

		CameraPosition cameraPosition = CameraPosition.builder()
		  .target(CIU)
		  .zoom(13)
		  .build();
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),2000, null);
		   
		return view;
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
