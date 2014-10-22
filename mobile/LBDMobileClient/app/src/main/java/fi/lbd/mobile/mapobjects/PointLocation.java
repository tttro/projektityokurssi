package fi.lbd.mobile.mapobjects;

import android.location.Location;
import android.util.Log;

import fi.lbd.mobile.repository.MapObjectParser;


/**
 * Unified simple location.
 *
 * Created by tommi on 19.10.2014.
 */
public interface PointLocation {
    double getLatitude();
    double getLongitude();
}
