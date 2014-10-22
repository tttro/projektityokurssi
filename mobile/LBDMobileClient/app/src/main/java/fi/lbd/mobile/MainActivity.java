package fi.lbd.mobile;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.squareup.otto.Subscribe;

import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestNearObjectsEvent;
import fi.lbd.mobile.events.RequestViewChangeEvent;
import fi.lbd.mobile.fragments.GoogleMapFragment;
import fi.lbd.mobile.fragments.MapFragment;
import fi.lbd.mobile.fragments.ObjectListFragment;
import fi.lbd.mobile.mapobjects.ImmutablePointLocation;
import fi.lbd.mobile.mapobjects.SelectionManager;
import fi.lbd.mobile.repository.MapObjectRepositoryService;

/**
 * MainActivity for the LBDMobileClient. Handles the RequestViewChangeEvents from the OTTO-bus.
 *
 * Created by tommi on 19.10.2014.
 */
public class MainActivity extends Activity {
    private MapFragment mapFragment;
    private SelectionManager selectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //this.mapFragment = new MapBoxFragment();
        this.mapFragment = new GoogleMapFragment();

        // Start the object repository service.
        startService(new Intent(this, MapObjectRepositoryService.class));

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            ObjectListFragment frag = new ObjectListFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, frag)
                    .commit();

            // Requests the default map objects from the object repository through OTTO-bus. TODO: Parempi tapa tehdä?
            AsyncTask task = new AsyncTask<Void,Void,Void>(){
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        // TODO: Exception handling...
                    }
                    return null;
                }
                @Override
                protected void onPostExecute(Void result) {
                    BusHandler.getBus().post(new RequestNearObjectsEvent(new ImmutablePointLocation(23.795199257764725, 61.503697166613755))); // TODO: Actual location
                }
            }.execute();
        }

    }

    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, MapObjectRepositoryService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusHandler.getBus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusHandler.getBus().unregister(this);
    }

    @Subscribe
    public void onEvent(RequestViewChangeEvent event) {
        switch (event.getViewType()) {
            case Map:
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, this.mapFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
        }
    }
}
