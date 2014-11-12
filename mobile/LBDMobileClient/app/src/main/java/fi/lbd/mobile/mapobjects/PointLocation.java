package fi.lbd.mobile.mapobjects;

import java.io.Serializable;


/**
 * Unified simple location.
 *
 * Created by tommi on 19.10.2014.
 */
public interface PointLocation extends Serializable {
    double getLatitude();
    double getLongitude();
}
