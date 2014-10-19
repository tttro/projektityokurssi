package fi.lbd.mobile.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import fi.lbd.mobile.BusHandler;
import fi.lbd.mobile.events.RequestViewChangeEvent;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.events.ReturnMapObjectsEvent;


/**
 * The map object list view. On click causes view change event to OTTO-bus.
 * Receives MapObjects from ObjectRepositoryService via OTTO-bus.
 *
 * Created by tommi on 19.10.2014.
 */
public class ObjectListFragment extends ListFragment {
    private ArrayAdapter<MapObject> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.adapter = new ArrayAdapter<MapObject>(getActivity(), android.R.layout.simple_list_item_1);
    }

    @Override
    public void onResume() {
        super.onResume();
        setListAdapter(this.adapter);
        BusHandler.BUS.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusHandler.BUS.unregister(this);
    }

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        BusHandler.BUS.post(new RequestViewChangeEvent(RequestViewChangeEvent.ViewType.Map, this.adapter.getItem(position)));
	}

    @Subscribe
    public void returnedMapObjects(ReturnMapObjectsEvent event) {
        this.adapter.clear();
        this.adapter.addAll(event.getMapObjects());
    }
} 
