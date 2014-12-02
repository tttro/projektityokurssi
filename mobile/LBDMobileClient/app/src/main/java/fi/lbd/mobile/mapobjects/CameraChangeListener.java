package fi.lbd.mobile.mapobjects;

import fi.lbd.mobile.location.PointLocation;

/**
 * Created by Tommi.
 */
public interface CameraChangeListener {
    void cameraChanged(double zoom, double startLat, double startLon, double endtLat, double endLon);
}
