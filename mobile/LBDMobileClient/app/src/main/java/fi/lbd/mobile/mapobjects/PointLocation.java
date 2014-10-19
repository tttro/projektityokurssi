package fi.lbd.mobile.mapobjects;

import android.location.Location;
import android.util.Log;

import fi.lbd.mobile.repository.MapObjectParser;


/**
 * Unified simple location.
 * TODO: Tämä vai androidin Location?
 *
 * Created by tommi on 19.10.2014.
 */
public class PointLocation {
    public final double latitude;
    public final double longitude;

    public PointLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "PointLocation, Latitude: "+ this.latitude +" Longitude: "+ this.longitude;
    }

    // String format: [21.134544,33.23312]
    public static PointLocation parseFromString(String latLng) {
        Log.i(MapObjectParser.class.getSimpleName(), "latLng: " + latLng);

        String[] parts = latLng.split("[\\[||\\,||\\]]");
        if (parts.length < 3) {
            //throw new MapObjectParseException("PointLocation is in wrong format!"); // TODO: Poikkeukset ja niiden käsittely...
            Log.e(MapObjectParser.class.getSimpleName(), "Mikään ei toimi!");
        }

        double longitude = Double.parseDouble(parts[1]);
        double latitude = Double.parseDouble(parts[2]);
        return new PointLocation(latitude,longitude);
    }
}
