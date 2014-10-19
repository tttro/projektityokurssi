package fi.lbd.mobile.repository;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.List;

import fi.lbd.mobile.BusHandler;
import fi.lbd.mobile.events.RequestNearObjectsEvent;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.PointLocation;
import fi.lbd.mobile.events.ReturnMapObjectsEvent;

/**
 * Service for handling the map object repository. Communication is done through OTTO-bus.
 *
 * Created by tommi on 19.10.2014.
 */
public class ObjectRepositoryService extends Service {
    private MapObjectRepository repo;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.repo = new MapObjectRepository();
        BusHandler.BUS.register(this);
//        Toast.makeText(this, "Service created", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusHandler.BUS.unregister(this);
//        Toast.makeText(this, "Service destroyed", Toast.LENGTH_LONG).show();
    }

    // TODO: Nyt taitaa olla tuo service aivan turha koska joka tapauksessa tunkkaa async taskilla pyörimään?
    // TODO: Täähän ois ihan sama ku joku normi objekti mikä tekis saman asian?
    // TODO: Pitänee muuttaa et tuottaa jotain taskeja queueen, jotai tausta service sit prosessoi...
    @Subscribe
    public void requestNearbyObjectsEvent(RequestNearObjectsEvent event) {
        Toast.makeText(this, "RequestNearObjectsEvent!", Toast.LENGTH_LONG).show();

        AsyncTask task = new AsyncTask<PointLocation,List<MapObject>,List<MapObject>>(){
            @Override
            protected List<MapObject> doInBackground(PointLocation... params) {
                return repo.getObjectsNearLocation(params[0]);
            }

            @Override
            protected void onPostExecute(List<MapObject> result) {
                BusHandler.BUS.post(new ReturnMapObjectsEvent(result));
            }
        }.execute(event.getLocation());
    }
}
