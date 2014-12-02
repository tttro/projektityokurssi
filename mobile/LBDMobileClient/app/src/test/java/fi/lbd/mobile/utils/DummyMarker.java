package fi.lbd.mobile.utils;

import java.util.EnumMap;

import fi.lbd.mobile.mapobjects.MarkerProducer;

/**
 * Created by Tommi.
 */
public class DummyMarker {
    private final String id;
    private final double latitude;
    private final double longitude;
    private final EnumMap<MarkerProducer.Event, Boolean> events;

    public DummyMarker(String id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.events = new EnumMap<MarkerProducer.Event, Boolean>(MarkerProducer.Event.class);
    }

    public String getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setEvent(MarkerProducer.Event evt) {
        this.events.put(evt, true);
    }

    public boolean getEvent(MarkerProducer.Event evt) {
        return this.events.get(evt) != null && this.events.get(evt).equals(true);
    }

    public void resetEvents() {
        this.events.clear();
    }
}
