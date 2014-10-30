package fi.lbd.mobile.fragments;

import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.R;
import fi.lbd.mobile.events.RequestNearObjectsEvent;
import fi.lbd.mobile.events.ReturnNearObjectsEvent;
import fi.lbd.mobile.mapobjects.ImmutablePointLocation;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.SelectionManager;


/**
 * The map object list view. On click causes view change event to OTTO-bus.
 * Receives MapObjects from ObjectRepositoryService via OTTO-bus.
 *
 * Created by tommi on 19.10.2014.
 */
public class ObjectListFragment extends ListFragment {
    private ListMapObjectAdapter adapter;
    private List<ListClickListener<MapObject>> listClickListeners = new ArrayList<>();

    public static ObjectListFragment newInstance() {
        return new ObjectListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.adapter = new ListMapObjectAdapter(this.getActivity());

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listview_fragment, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setListAdapter(this.adapter);
        BusHandler.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusHandler.getBus().unregister(this);
    }

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        SelectionManager.get().setSelection(this.adapter.get(position));
        notifyClickListeners(this.adapter.get(position));
	}

    @Subscribe
    public void onEvent(ReturnNearObjectsEvent event) {
        this.adapter.clear();
        this.adapter.addAll(event.getMapObjects());
        this.getListView().requestLayout();
    }

    public void addListClickListener(ListClickListener<MapObject> listener) {
        this.listClickListeners.add(listener);
    }

    private void notifyClickListeners(MapObject object) {
        for(ListClickListener<MapObject> listener : this.listClickListeners) {
            listener.onClick(object);
        }
    }
} 
