package fi.lbd.mobile.location;

import android.location.Location;

import java.text.DecimalFormat;

import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Contains utility methods for locations.
 *
 * Created by Tommi.
 */
public class LocationUtils {
    private static DecimalFormat df = new DecimalFormat("#.00");

    /**
     * Returns distance between two points in meters.
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    public static double distanceBetween(double lat1, double lon1, double lat2, double lon2) {
        Location l1 = new Location("1");
        l1.setLatitude(lat1);
        l1.setLongitude(lon1);

        Location l2 = new Location("2");
        l2.setLatitude(lat2);
        l2.setLongitude(lon2);

        return l1.distanceTo(l2);
    }

    /**
     * Returns distance between two points in meters.
     *
     * @param loc1
     * @param loc2
     * @return
     */
    public static double distanceBetween(PointLocation loc1, PointLocation loc2) {
        Location l1 = new Location("1");
        l1.setLatitude(loc1.getLatitude());
        l1.setLongitude(loc1.getLongitude());

        Location l2 = new Location("2");
        l2.setLatitude(loc2.getLatitude());
        l2.setLongitude(loc2.getLongitude());

        return l1.distanceTo(l2);
    }

    /**
     * Returns distance between two map objects in meters.
     *
     * @param obj1
     * @param obj2
     * @return
     */
    public static double distanceBetween(MapObject obj1, MapObject obj2) {
        return LocationUtils.distanceBetween(obj1.getPointLocation(), obj2.getPointLocation());
    }


    /**
     * Formats the given distance into string.
     *
     * @param distanceInMeters  For example 123m if distanceInMeters < 1000. For example 2.36km if
     *                          distanceInMeters > 1000m.
     * @return
     */
    public static String formatDistance(double distanceInMeters) {
        if (distanceInMeters < 1000) {
            return (int)distanceInMeters +"m";
        } else {
            return df.format(distanceInMeters/1000) +"km";
        }
    }
}
