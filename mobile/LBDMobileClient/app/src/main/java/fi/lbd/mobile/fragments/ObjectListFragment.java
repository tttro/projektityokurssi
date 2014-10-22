package fi.lbd.mobile.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.R;
import fi.lbd.mobile.events.RequestViewChangeEvent;
import fi.lbd.mobile.events.ReturnNearObjectsEvent;
import fi.lbd.mobile.mapobjects.SelectionManager;


/**
 * The map object list view. On click causes view change event to OTTO-bus.
 * Receives MapObjects from ObjectRepositoryService via OTTO-bus.
 *
 * Created by tommi on 19.10.2014.
 */
public class ObjectListFragment extends ListFragment {
    private ListMapObjectAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.adapter = new ListMapObjectAdapter(this.getActivity());
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
        BusHandler.getBus().post(new RequestViewChangeEvent(RequestViewChangeEvent.ViewType.Map));
	}

    @Subscribe
    public void onEvent(ReturnNearObjectsEvent event) {
        this.adapter.clear();
        this.adapter.addAll(event.getMapObjects());
        this.getListView().requestLayout();
        Toast.makeText(this.getActivity(), "returnedMapObjects: count: "+event.getMapObjects().size(), Toast.LENGTH_LONG).show();
    }
} 
