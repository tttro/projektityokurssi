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

import fi.lbd.mobile.R;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.events.RequestNearObjectsEvent;
import fi.lbd.mobile.events.ReturnNearObjectsEvent;
import fi.lbd.mobile.mapobjects.ImmutablePointLocation;
import fi.lbd.mobile.mapobjects.MapObject;
import fi.lbd.mobile.mapobjects.SelectionManager;



public class InboxFragment extends ListFragment {
    private ListMapObjectAdapter adapter;

    public static InboxFragment newInstance() {
        return new InboxFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.adapter = new ListMapObjectAdapter(this.getActivity()); // TODO: Oma adapteri.

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
	}

} 
