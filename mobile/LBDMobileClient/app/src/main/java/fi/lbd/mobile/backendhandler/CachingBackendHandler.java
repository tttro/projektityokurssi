package fi.lbd.mobile.backendhandler;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.location.PointLocation;

/**
 * Caches the method invocations so that we don't need to fetch new data from the backend if the
 * same method is invoked multiple times inside a short time period.
 *
 * Created by Tommi.
 */
public class CachingBackendHandler extends BasicBackendHandler {
    private static final long MAX_CACHE_TIME = 1000 * 60 * 1; // 2 Min TODO: Pois staticista?

    private class CachedQuery {
        private long cacheTime;
        private List<MapObject> objects;

        private CachedQuery(long cacheTime, List<MapObject> objects) {
            this.cacheTime = cacheTime;
            this.objects = objects;
        }

        public long getCacheTime() {
            return cacheTime;
        }

        public List<MapObject> getObjects() {
            return objects;
        }
    }

    private ConcurrentHashMap<String, CachedQuery> cachedQueries = new ConcurrentHashMap<>();

    public CachingBackendHandler(String baseUrl, String dataSource) {
        super(baseUrl, dataSource);
    }


    /**
     * Returns the objects inside the area from the backend or as a cached result.
     *
     * @param southWest Start point.
     * @param northEast End point.
     * @param mini  Should the results be in minimized format.
     * @return
     */
    @Override
    public HandlerResponse getObjectsInArea(PointLocation southWest, PointLocation northEast, boolean mini) {
        String hash = "inarea/"+southWest.getLongitude()+","+southWest.getLatitude()+","+northEast.getLatitude()+","+northEast.getLongitude();
        CachedQuery cached = this.cachedQueries.get(hash);
        if (cached == null) {
            HandlerResponse response = super.getObjectsInArea(southWest, northEast, mini);

            // If the connection succeeded, cache the results.
            if (response.isOk()) {
                this.cachedQueries.put(hash, new CachedQuery(System.currentTimeMillis(), response.getObjects()));
            }
            // Return the results
            return response;
        } else {
            return new HandlerResponse(cached.getObjects(), HandlerResponse.Status.Cached);
        }
    }

    /**
     * Method which should be invoked frequently to check for outdated caches.
     */
    public void checkForOutdatedCaches() {
        Iterator<CachedQuery> iter = this.cachedQueries.values().iterator();
        while (iter.hasNext()) {
            CachedQuery query = iter.next();
            if (System.currentTimeMillis() - query.cacheTime > MAX_CACHE_TIME) {
                iter.remove();
            }
        }
    }
}
