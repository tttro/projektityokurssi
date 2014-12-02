package fi.lbd.mobile.utils;

import java.util.ArrayList;
import java.util.List;

import fi.lbd.mobile.mapobjects.MapObject;

/**
 * Helper class used in tests.
 *
 * Created by Tommi.
 */
public class Area {
    private double latGridStart;
    private double lonGridStart;
    private double latGridEnd;
    private double lonGridEnd;

    public Area(double latGridStart, double lonGridStart, double latGridEnd, double lonGridEnd) {
        this.latGridStart = latGridStart;
        this.lonGridStart = lonGridStart;
        this.latGridEnd = latGridEnd;
        this.lonGridEnd = lonGridEnd;
    }

    public double getLatGridStart() {
        return latGridStart;
    }

    public double getLonGridStart() {
        return lonGridStart;
    }

    public double getLatGridEnd() {
        return latGridEnd;
    }

    public double getLonGridEnd() {
        return lonGridEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Area area = (Area) o;

        if (Double.compare(area.latGridEnd, latGridEnd) != 0) return false;
        if (Double.compare(area.latGridStart, latGridStart) != 0) return false;
        if (Double.compare(area.lonGridEnd, lonGridEnd) != 0) return false;
        if (Double.compare(area.lonGridStart, lonGridStart) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latGridStart);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lonGridStart);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latGridEnd);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lonGridEnd);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
