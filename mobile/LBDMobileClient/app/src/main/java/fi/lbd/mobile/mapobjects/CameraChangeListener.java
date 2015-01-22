package fi.lbd.mobile.mapobjects;

import fi.lbd.mobile.location.PointLocation;

/**
 * Listens for map camera changes.
 *
 * Created by Tommi.
 */
public interface CameraChangeListener {
    /**
     * Method which is invoked on camera change event.
     * @param zoom
     * @param startLat
     * @param startLon
     * @param endtLat
     * @param endLon
     */
    void cameraChanged(double zoom, double startLat, double startLon, double endtLat, double endLon);
}
