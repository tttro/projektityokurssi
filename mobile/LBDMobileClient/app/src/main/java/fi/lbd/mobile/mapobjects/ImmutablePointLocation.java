package fi.lbd.mobile.mapobjects;

import android.util.Log;

import fi.lbd.mobile.repository.MapObjectParser;

/**
 * Created by tommi on 22.10.2014.
 */
public class ImmutablePointLocation implements PointLocation {
    private final double latitude;
    private final double longitude;

    public ImmutablePointLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public double getLatitude() {
        return this.latitude;
    }

    @Override
    public double getLongitude() {
        return this.longitude;
    }

    @Override
    public String toString() {
        return "Latitude: "+ this.latitude +", Longitude: "+ this.longitude;
    }

    // String format: [21.134544,33.23312]
    public static ImmutablePointLocation parseFromString(String latLng) {
        Log.i(MapObjectParser.class.getSimpleName(), "latLng: " + latLng);

        String[] parts = latLng.split("[\\[||\\,||\\]]");
        if (parts.length < 3) {
            //throw new MapObjectParseException("PointLocation is in wrong format!"); // TODO: Poikkeukset ja niiden käsittely...
            Log.e(MapObjectParser.class.getSimpleName(), "Mikään ei toimi!");
        }

        double longitude = Double.parseDouble(parts[1]);
        double latitude = Double.parseDouble(parts[2]);
        return new ImmutablePointLocation(latitude,longitude);
    }
}
