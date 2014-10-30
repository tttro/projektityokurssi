package fi.lbd.mobile.fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fi.lbd.mobile.R;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestObjectsInAreaEvent;
import fi.lbd.mobile.events.ReturnObjectsInAreaEvent;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.PointLocation;
import fi.lbd.mobile.mapobjects.SelectionManager;

// http://stackoverflow.com/questions/13713066/google-maps-android-api-v2-very-slow-when-adding-lots-of-markers
public class GoogleMapFragment extends MapFragment {
	private MapView mapView;
	private GoogleMap map;

    // TODO: Joku grid model?
    private List<Marker> currentMarkers = new ArrayList<Marker>();

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.googlemap_fragment, container, false);
		this.mapView = (MapView)view.findViewById(R.id.mapview);
        this.mapView.onCreate(savedInstanceState);

        this.map = this.mapView.getMap();
        this.map.getUiSettings().setMyLocationButtonEnabled(false);
        this.map.setMyLocationEnabled(true);
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
                coordinatesField.setText("["+marker.getPosition().latitude +", "+ marker.getPosition().longitude +"]");

                TextView infoField = (TextView) v.findViewById(R.id.info);
                infoField.setText(Html.fromHtml(marker.getSnippet()));
                return v;

            }

        });

        MapsInitializer.initialize(this.getActivity());

        MapObject selectedObject = SelectionManager.get().getSelectedObject();
        if (selectedObject != null) {
            PointLocation location = selectedObject.getPointLocation();
            CameraUpdate cameraLocation = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
            CameraUpdate cameraZoom = CameraUpdateFactory.zoomTo(15);
            this.map.moveCamera(cameraLocation);
            this.map.animateCamera(cameraZoom);
        }

        requestMapObjects(this.map.getProjection());
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

    private void requestMapObjects(Projection cameraProjection) {
        // TODO: Kameran projektion avulla tulisi määrittää alue, jolle haetaan karttakohteet
        // TODO: Markerit tulisi jakaa jonkinlaiseen grid modeliin, jonka soluja päivitetään kun kamera liikkuu
        //cameraProjection.getVisibleRegion().
        BusHandler.getBus().post(new RequestObjectsInAreaEvent());
    }

    @Subscribe
    public void onEvent(ReturnObjectsInAreaEvent event) {
        if (event.getMapObjects() != null) {
            Toast.makeText(this.getActivity(), "ReturnObjectsInAreaEvent: count: " + event.getMapObjects().size(), Toast.LENGTH_LONG).show();
            for (Marker marker : this.currentMarkers) {
                marker.remove();
            }
            this.currentMarkers.clear();

            for (MapObject mapObject : event.getMapObjects()) {
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(android.R.drawable.presence_invisible);
                LatLng location = new LatLng(mapObject.getPointLocation().getLatitude(),
                        mapObject.getPointLocation().getLongitude());
                if (SelectionManager.get().getSelectedObject() != null &&
                        mapObject.getId().equals(SelectionManager.get().getSelectedObject().getId())) {
                    icon = BitmapDescriptorFactory.fromResource(android.R.drawable.presence_online);
                }
                StringBuilder snippet = new StringBuilder();
                for (Map.Entry<String, String> entry : mapObject.getAdditionalProperties().entrySet()) {
                    snippet.append("<b>");
                    snippet.append(entry.getKey());
                    snippet.append(": ");
                    snippet.append("</b>");
                    snippet.append(entry.getValue());
                    snippet.append("<br>");
                }
                snippet.append("<br><b><font color=\"blue\">Click for detailed info.</font></b><br>");
                Marker marker = map.addMarker(
                        new MarkerOptions()
                                .position(location)
                                .title(mapObject.getId())
                                .snippet(snippet.toString())
                                .icon(icon));
//                GroundOverlay groundOverlay = map.addGroundOverlay(new GroundOverlayOptions()
//                        .image(image)
//                        .positionFromBounds(bounds)
//                        .transparency(0.5));
            }
        }
    }
}
