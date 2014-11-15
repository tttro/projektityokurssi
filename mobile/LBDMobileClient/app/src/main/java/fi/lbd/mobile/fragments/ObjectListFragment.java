package fi.lbd.mobile.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import android.widget.ExpandableListAdapter;

import fi.lbd.mobile.LBDApplication;
import fi.lbd.mobile.adapters.ListExpandableAdapter;
import fi.lbd.mobile.adapters.ListMapObjectAdapter;
import fi.lbd.mobile.events.BusHandler;
import fi.lbd.mobile.R;
import fi.lbd.mobile.events.RequestNearObjectsEvent;
import fi.lbd.mobile.events.ReturnNearObjectsEvent;
import fi.lbd.mobile.mapobjects.ImmutablePointLocation;
import fi.lbd.mobile.mapobjects.MapObject;


/**
 * The map object list view. On click causes view change event to OTTO-bus.
 * Receives MapObjects from ObjectRepositoryService via OTTO-bus.
 *
 * Created by tommi on 19.10.2014.
 */
public class ObjectListFragment extends ListFragment {
    private ListExpandableAdapter adapter;
    private List<ListClickListener<MapObject>> listClickListeners = new ArrayList<>();
    private ExpandableListView expview;
    private EditText searchText;
    public static ObjectListFragment newInstance() {
        return new ObjectListFragment();
    }

    private int firstVisiblePosition;
    private ArrayList<Boolean> groupExpandedArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.adapter = new ListExpandableAdapter(this.getActivity());
        this.groupExpandedArray = new ArrayList<Boolean>();
        this.firstVisiblePosition = 0;

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
                BusHandler.getBus().post(new RequestNearObjectsEvent(new ImmutablePointLocation(61.510988, 23.777366), 0.001)); // TODO: Actual location
            }
        }.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listview_search_fragment, container, false);
        this.expview = (ExpandableListView) view.findViewById(android.R.id.list);
        this.searchText = (EditText)view.findViewById(R.id.searchText);

        // TODO: Search-toiminnallisuus
        view.findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyBoard();
                hideCursor();
            }
        });

        // Hide keyboard and blinking cursor when "enter" or "back" key is pressed on soft keyboard
        this.searchText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    hideKeyBoard();
                    hideCursor();
                    return true;
                }
                else if(keyCode == KeyEvent.KEYCODE_BACK) {
                    hideKeyBoard();
                    hideCursor();
                    return true;
                }
                else {
                    return false;
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.expview.setAdapter(this.adapter);

        BusHandler.getBus().register(this);

        for (int i=0; i<groupExpandedArray.size() ;i++){
            if (groupExpandedArray.get(i) == true)
                expview.expandGroup(i);
        }
        this.expview.setSelection(firstVisiblePosition );
    }

    @Override
    public void onPause() {
        super.onPause();
        BusHandler.getBus().unregister(this);

        int numberOfGroups = adapter.getGroupCount();

        this.groupExpandedArray.clear();
        for (int i=0;i<numberOfGroups;i++){
            this.groupExpandedArray.add(this.expview.isGroupExpanded(i));
        }
        this.firstVisiblePosition = this.expview.getFirstVisiblePosition();
    }

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //SelectionManager.get().setSelection(this.adapter.get(position));
        notifyClickListeners((MapObject)this.adapter.getGroup(position));
	}

    @Subscribe
    public void onEvent(ReturnNearObjectsEvent event) {
        this.adapter.clear();
        if (event.getMapObjects() != null) {
            this.adapter.addAll(event.getMapObjects());
        }
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

    public void hideKeyBoard() {
        if (this.searchText != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    getActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.searchText.getWindowToken(), 0);
        }
    }

    public void hideCursor(){
        if(this.searchText != null) {
            this.searchText.setFocusable(false);
            this.searchText.setFocusableInTouchMode(true);
        }
    }
} 
