package fi.lbd.mobile.backendhandler;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.PointLocation;

/**
 * Created by tommi on 10.11.2014.
 */
public class CachingBackendHandler extends BasicBackendHandler {
    // TODO: Tartteeko jotain spatiaalista cachea? http://www.geotools.org/ tai joku vastaava

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


    @Override
    public List<MapObject> getObjectsInArea(PointLocation southWest, PointLocation northEast, boolean mini) {
        String hash = "inarea/"+southWest.getLongitude()+","+southWest.getLatitude()+","+northEast.getLatitude()+","+northEast.getLongitude();
        CachedQuery cached = this.cachedQueries.get(hash);
        if (cached == null) {
            List<MapObject> objects = super.getObjectsInArea(southWest, northEast, mini);
            this.cachedQueries.put(hash, new CachedQuery(System.currentTimeMillis(), objects));
            return objects;
        } else {
            return cached.getObjects();
        }

//        if (cached == null || System.currentTimeMillis()-cached.cacheTime > MAX_CACHE_TIME) {
//            List<MapObject> objects = super.getObjectsInArea(southWest, northEast, mini);
//            this.cachedQueries.put(hash, new CachedQuery(System.currentTimeMillis(), objects));
//            return objects;
//        } else {
////            Log.e("MapObjectRepository", "Using cached query: "+ (int)((System.currentTimeMillis() - cached.cacheTime)/1000) +"s old." );
//            return cached.getObjects();
//        }
    }

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
