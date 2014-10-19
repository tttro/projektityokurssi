package fi.lbd.mobile;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;

import fi.lbd.mobile.events.RequestNearObjectsEvent;
import fi.lbd.mobile.events.RequestViewChangeEvent;
import fi.lbd.mobile.fragments.MapBoxFragment;
import fi.lbd.mobile.fragments.MapFragment;
import fi.lbd.mobile.fragments.ObjectListFragment;
import fi.lbd.mobile.mapobjects.PointLocation;
import fi.lbd.mobile.repository.ObjectRepositoryService;

/**
 * MainActivity for the LBDMobileClient. Handles the RequestViewChangeEvents from the OTTO-bus.
 *
 * Created by tommi on 19.10.2014.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start the object repository service.
        Intent intent = new Intent(this, ObjectRepositoryService.class);
        startService(intent);

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            ObjectListFragment frag = new ObjectListFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, frag)
                    .commit();

            // Requests the default map objects from the object repository through OTTO-bus. TODO: Better ways to do this?
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
                    BusHandler.BUS.post(new RequestNearObjectsEvent(new PointLocation(23.795199257764725,61.503697166613755))); // TODO: Actual location
                }
            }.execute();
        }

    }

    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, ObjectRepositoryService.class);
        stopService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusHandler.BUS.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusHandler.BUS.unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Subscribe
    public void changeView(RequestViewChangeEvent event) {
        switch (event.getViewType()) {
            case Map:
                // TODO: Reusing instances between view changes?
                MapFragment frag = new MapBoxFragment();
                //MapFragment frag = new GoogleMapFragment();
                frag.setCurrentMapObjects(event.getMapObjects());

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, frag);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
        }
    }
}
