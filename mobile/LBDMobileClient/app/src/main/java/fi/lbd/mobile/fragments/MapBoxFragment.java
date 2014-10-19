package fi.lbd.mobile.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.ItemizedOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.views.MapView;

import java.util.ArrayList;
import java.util.List;

import fi.lbd.mobile.R;
import fi.lbd.mobile.mapobjects.MapObject;

// TODO: Kesken koko luokka
// https://github.com/mapbox/mapbox-android-sdk/blob/mb-pages/QUICKSTART.md#on-runtime
public class MapBoxFragment extends MapFragment {
    private UserLocationOverlay myLocationOverlay;
    private MapView mapView;
//    private String currentMap;
    private List<MapObject> currentObjects = new ArrayList<MapObject>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//View view = inflater.inflate(R.layout.mapbox_fragment, container, false);

        mapView = new MapView(inflater.getContext());
        mapView.setTileSource(new MapboxTileLayer(getString(R.string.MapBoxApiKey)));
//
//        final LatLng location = new LatLng(getArguments().getDouble("latitude"), getArguments().getDouble("longitude"));

        //mapView = (MapView) view.findViewById(R.id.mapview);
        mapView.setMinZoomLevel(mapView.getTileProvider().getMinimumZoomLevel());
        mapView.setMaxZoomLevel(mapView.getTileProvider().getMaximumZoomLevel());
       // mapView.setCenter(mapView.getTileProvider().getCenterCoordinate());
        mapView.setDiskCacheEnabled(true);
       // mapView.setZoom(0);
//        currentMap = getString(R.string.streetMapId);

//        mapView.setCenter(location);
//        myLocationOverlay = new UserLocationOverlay(new GpsLocationProvider(this.getActivity()), mapView);
//        myLocationOverlay.setDrawAccuracyEnabled(true);
//        mapView.getOverlays().add(myLocationOverlay);

//        Marker marker = new Marker(mapView, "Tolppa", "Superhieno katulamppu", location);
//        mapView.addMarker(marker);

        boolean first = true;
        for (MapObject obj : this.currentObjects) {
            LatLng location = new LatLng(obj.getPointLocation().latitude, obj.getPointLocation().longitude);
            Marker marker = new Marker(mapView, "Tolppa", "Superhieno katulamppu", location);
            mapView.addMarker(marker);

            if (first) {
                mapView.setCenter(location);
                first = false;
            }
        }
        return mapView;
	}

    @Override
    public void setCurrentMapObjects(List<MapObject> currentObjects) {
        this.currentObjects.clear();
        this.currentObjects.addAll(currentObjects);
    }

//    protected void replaceMapView(String layer) {
//        if (TextUtils.isEmpty(layer) || TextUtils.isEmpty(currentMap) || currentMap.equalsIgnoreCase(layer)) {
//            return;
//        }
//        ITileLayer source;
//        BoundingBox box;
//        source = new MapboxTileLayer(layer);
//        mapView.setTileSource(source);
//        box = source.getBoundingBox();
//        mapView.setScrollableAreaLimit(box);
//        mapView.setMinZoomLevel(mapView.getTileProvider().getMinimumZoomLevel());
//        mapView.setMaxZoomLevel(mapView.getTileProvider().getMaximumZoomLevel());
//        currentMap = layer;
//
//    }


	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}
}
