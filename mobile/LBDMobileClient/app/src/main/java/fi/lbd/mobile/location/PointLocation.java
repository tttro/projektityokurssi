package fi.lbd.mobile.location;

import java.io.Serializable;


/**
 * Interface for simple point location.
 *
 * Created by Tommi.
 */
public interface PointLocation extends Serializable {
    double getLatitude();
    double getLongitude();
}
