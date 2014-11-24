package fi.lbd.mobile.location;

import android.util.Log;

import fi.lbd.mobile.backendhandler.MapObjectParser;

/**
 * Point location which cannot be modified.
 *
 * Created by Tommi.
 */
public class ImmutablePointLocation implements PointLocation {
    private final double latitude;
    private final double longitude;

    /**
     * Point location which cannot be modified.
     *
     * @param latitude
     * @param longitude
     */
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

    // Generated equals and hash:
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImmutablePointLocation that = (ImmutablePointLocation) o;

        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Double.compare(that.longitude, longitude) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
